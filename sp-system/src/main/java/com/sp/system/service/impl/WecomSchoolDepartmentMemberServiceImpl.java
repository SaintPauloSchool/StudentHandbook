package com.sp.system.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sp.common.utils.http.HttpUtils;
import com.sp.system.entity.WecomSchoolDepartmentMember;
import com.sp.system.mapper.WecomSchoolDepartmentMemberMapper;
import com.sp.system.service.WecomSchoolDepartmentMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 企業微信學校部門成員業務邏輯實現類
 */
@Service
public class WecomSchoolDepartmentMemberServiceImpl implements WecomSchoolDepartmentMemberService {

    private static final Logger logger = LoggerFactory.getLogger(WecomSchoolDepartmentMemberServiceImpl.class);

    @Autowired
    private WecomSchoolDepartmentMemberMapper wecomSchoolDepartmentMemberMapper;

    /**
     * 根據部門 ID 數組獲取並同步所有成員數據
     *
     * @param departmentIdArray 部門 ID 數組
     * @param accessToken access_token
     * @return 總共獲取到的成員數量
     */
    @Override
    @Transactional
    public int fetchAndSaveAllMembers(JSONArray departmentIdArray, String accessToken) {
        if (CollectionUtils.isEmpty(departmentIdArray)) {
            logger.info("部門 ID 數組爲空，跳過成員同步");
            return 0;
        }
        
        int totalMembers = 0;
        
        // 遍歷部門 ID 數組
        for (int i = 0; i < departmentIdArray.size(); i++) {
            // 獲取部門對象
            JSONObject deptObj = departmentIdArray.getJSONObject(i);
            if (deptObj == null) {
                continue;
            }
            
            Long departmentId = deptObj.getLong("id");
            if (departmentId == null) {
                continue;
            }
            
            try {
                // 獲取部門成員列表
                JSONArray userList = fetchMemberList(departmentId, accessToken);
                // 同步成員數據（增刪改）
                if (!CollectionUtils.isEmpty(userList)) {
                    // 同步成員數據
                    syncMembers(userList, departmentId);
                    logger.info("部門 ID: {} - 獲取到 {} 個成員，已同步", departmentId, userList.size());
                    totalMembers += userList.size();
                }
            } catch (Exception e) {
                logger.error("獲取部門 {} 的成員列表失敗", departmentId, e);
            }
        }
        
        return totalMembers;
    }

    /**
     * 獲取部門成員列表
     */
    private JSONArray fetchMemberList(Long departmentId, String accessToken) {
        // 獲取部門成員列表
        String userUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/simplelist?access_token=" + accessToken + "&department_id=" + departmentId;
        String userResponse = HttpUtils.sendGet(userUrl);
        // 解析結果
        JSONObject userResult = JSONObject.parseObject(userResponse);
        // 檢查結果
        if (userResult.getInteger("errcode") == null || userResult.getInteger("errcode") != 0) {
            logger.error("獲取部門 {} 的成員列表失敗：errcode={}, errmsg={}", 
                    departmentId, userResult.getInteger("errcode"), userResult.getString("errmsg"));
            return null;
        }
        
        return userResult.getJSONArray("userlist");
    }


    /**
     * 根據部門 ID 查詢成員列表
     *
     * @param departmentId 部門 ID
     * @return 成員列表
     */
    @Override
    public List<WecomSchoolDepartmentMember> getMembersByDepartmentId(Long departmentId) {
        return wecomSchoolDepartmentMemberMapper.selectByDepartmentId(departmentId);
    }

    /**
     * 同步部門成員數據（以企業微信數據為準）
     * @param wechatMembers 企業微信成員列表
     * @param departmentId 部門 ID
     */
    @Override
    @Transactional
    public void syncMembers(JSONArray wechatMembers, Long departmentId) {
        if (CollectionUtils.isEmpty(wechatMembers)) {
            logger.info("部門 {} 的企業微信成員數據爲空，跳過同步", departmentId);
            return;
        }
        
        // 獲取數據庫中該部門的所有成員
        List<WecomSchoolDepartmentMember> dbMembers = getMembersByDepartmentId(departmentId);
        
        // 將數據庫成員列錶轉換爲 Map，方便對比
        Map<String, WecomSchoolDepartmentMember> dbMemberMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(dbMembers)) {
            dbMemberMap = dbMembers.stream()
                    .collect(Collectors.toMap(WecomSchoolDepartmentMember::getUserid, member -> member));
        }
        
        // 標記需要刪除的成員（初始爲數據庫中的所有成員）
        Set<String> membersToDelete = new HashSet<>();
        if (!CollectionUtils.isEmpty(dbMembers)) {
            membersToDelete = dbMembers.stream()
                    .map(WecomSchoolDepartmentMember::getUserid)
                    .collect(Collectors.toSet());
        }
        
        // 解析企業微信成員數據
        List<WecomSchoolDepartmentMember> wechatMemberList = parseMembers(wechatMembers, departmentId);
        
        // 分類處理：新增和更新
        List<WecomSchoolDepartmentMember> membersToInsert = new ArrayList<>();
        List<WecomSchoolDepartmentMember> membersToUpdate = new ArrayList<>();
        
        for (WecomSchoolDepartmentMember wechatMember : wechatMemberList) {
            String userid = wechatMember.getUserid();
            
            // 從待刪除列表中移除（因爲這個成員在企業微信中存在）
            membersToDelete.remove(userid);
            
            // 判斷是新增還是更新
            if (dbMemberMap.containsKey(userid)) {
                membersToUpdate.add(wechatMember);
            } else {
                membersToInsert.add(wechatMember);
            }
        }
        
        logger.info("部門 {} 成員同步 - 新增：{}, 更新：{}, 刪除：{}", 
                departmentId, membersToInsert.size(), membersToUpdate.size(), membersToDelete.size());
        
        // 執行批量操作
        executeBatchOperations(membersToInsert, membersToUpdate, membersToDelete, departmentId);
    }

    /**
     * 解析企業微信成員數據
     * @param memberArray 成員數組
     * @param departmentId 部門 ID
     * @return 成員對象列表
     */
    private List<WecomSchoolDepartmentMember> parseMembers(JSONArray memberArray, Long departmentId) {
        // 創建成員對象列表
        List<WecomSchoolDepartmentMember> members = new ArrayList<>();
        // 如果成員數組爲空，則返回空列表
        if (CollectionUtils.isEmpty(memberArray)) {
            return members;
        }
        // 遍歷成員數組
        for (int i = 0; i < memberArray.size(); i++) {
            // 獲取成員對象
            JSONObject memberObj = memberArray.getJSONObject(i);
            if (memberObj == null) {
                continue;
            }
            // 創建成員對象
            WecomSchoolDepartmentMember member = new WecomSchoolDepartmentMember();
            member.setUserid(memberObj.getString("userid"));
            member.setDepartmentId(departmentId);
            member.setName(memberObj.getString("name"));
            member.setOpenUserid(memberObj.getString("open_userid"));
            // 添加到列表中
            members.add(member);
        }
        // 返回成員列表
        return members;
    }

    /**
     * 執行批量操作（新增、更新、刪除）
     * @param toInsert 待新增的成員列表
     * @param toUpdate 待更新的成員列表
     * @param toDelete 待刪除的用戶 ID 集合
     * @param departmentId 部門 ID
     */
    private void executeBatchOperations(List<WecomSchoolDepartmentMember> toInsert, 
                                        List<WecomSchoolDepartmentMember> toUpdate,
                                        Set<String> toDelete,
                                        Long departmentId) {
        // 執行批量新增
        if (!toInsert.isEmpty()) {
            wecomSchoolDepartmentMemberMapper.batchInsertSchoolDepartmentMembers(toInsert);
            logger.debug("部門 {} 批量新增成員 {} 個", departmentId, toInsert.size());
        }
        
        // 執行批量更新
        if (!toUpdate.isEmpty()) {
            for (WecomSchoolDepartmentMember member : toUpdate) {
                wecomSchoolDepartmentMemberMapper.updateSchoolDepartmentMember(member);
            }
            logger.debug("部門 {} 批量更新成員 {} 個", departmentId, toUpdate.size());
        }
        
        // 執行批量刪除
        if (!toDelete.isEmpty()) {
            for (String userid : toDelete) {
                wecomSchoolDepartmentMemberMapper.deleteSchoolDepartmentMemberByUseridAndDepartmentId(userid, departmentId);
            }
            logger.debug("部門 {} 批量刪除成員 {} 個", departmentId, toDelete.size());
        }
    }
}

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
 * 企业微信学校部门成员业务逻辑实现类
 */
@Service
public class WecomSchoolDepartmentMemberServiceImpl implements WecomSchoolDepartmentMemberService {

    private static final Logger logger = LoggerFactory.getLogger(WecomSchoolDepartmentMemberServiceImpl.class);

    @Autowired
    private WecomSchoolDepartmentMemberMapper wecomSchoolDepartmentMemberMapper;

    /**
     * 根据部门 ID 数组获取并同步所有成员数据
     *
     * @param departmentIdArray 部门 ID 数组
     * @param accessToken access_token
     * @return 总共获取到的成员数量
     */
    @Override
    @Transactional
    public int fetchAndSaveAllMembers(JSONArray departmentIdArray, String accessToken) {
        if (CollectionUtils.isEmpty(departmentIdArray)) {
            logger.info("部门 ID 数组为空，跳过成员同步");
            return 0;
        }
        
        int totalMembers = 0;
        
        // 遍历部门 ID 数组
        for (int i = 0; i < departmentIdArray.size(); i++) {
            // 获取部门对象
            JSONObject deptObj = departmentIdArray.getJSONObject(i);
            if (deptObj == null) {
                continue;
            }
            
            Long departmentId = deptObj.getLong("id");
            if (departmentId == null) {
                continue;
            }
            
            try {
                // 获取部门成员列表
                JSONArray userList = fetchMemberList(departmentId, accessToken);
                // 同步成员数据（增删改）
                if (!CollectionUtils.isEmpty(userList)) {
                    // 同步成员数据
                    syncMembers(userList, departmentId);
                    logger.info("部门 ID: {} - 获取到 {} 个成员，已同步", departmentId, userList.size());
                    totalMembers += userList.size();
                }
            } catch (Exception e) {
                logger.error("获取部门 {} 的成员列表失败", departmentId, e);
            }
        }
        
        return totalMembers;
    }

    /**
     * 获取部门成员列表
     */
    private JSONArray fetchMemberList(Long departmentId, String accessToken) {
        // 获取部门成员列表
        String userUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/simplelist?access_token=" + accessToken + "&department_id=" + departmentId;
        String userResponse = HttpUtils.sendGet(userUrl);
        // 解析结果
        JSONObject userResult = JSONObject.parseObject(userResponse);
        // 检查结果
        if (userResult.getInteger("errcode") == null || userResult.getInteger("errcode") != 0) {
            logger.error("获取部门 {} 的成员列表失败：errcode={}, errmsg={}", 
                    departmentId, userResult.getInteger("errcode"), userResult.getString("errmsg"));
            return null;
        }
        
        return userResult.getJSONArray("userlist");
    }


    /**
     * 根据部门 ID 查询成员列表
     *
     * @param departmentId 部门 ID
     * @return 成员列表
     */
    @Override
    public List<WecomSchoolDepartmentMember> getMembersByDepartmentId(Long departmentId) {
        return wecomSchoolDepartmentMemberMapper.selectByDepartmentId(departmentId);
    }

    /**
     * 同步部门成员数据（以企业微信数据为准）
     * @param wechatMembers 企业微信成员列表
     * @param departmentId 部门 ID
     */
    @Override
    @Transactional
    public void syncMembers(JSONArray wechatMembers, Long departmentId) {
        if (CollectionUtils.isEmpty(wechatMembers)) {
            logger.info("部门 {} 的企业微信成员数据为空，跳过同步", departmentId);
            return;
        }
        
        // 获取数据库中该部门的所有成员
        List<WecomSchoolDepartmentMember> dbMembers = getMembersByDepartmentId(departmentId);
        
        // 将数据库成员列表转换为 Map，方便对比
        Map<String, WecomSchoolDepartmentMember> dbMemberMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(dbMembers)) {
            dbMemberMap = dbMembers.stream()
                    .collect(Collectors.toMap(WecomSchoolDepartmentMember::getUserid, member -> member));
        }
        
        // 标记需要删除的成员（初始为数据库中的所有成员）
        Set<String> membersToDelete = new HashSet<>();
        if (!CollectionUtils.isEmpty(dbMembers)) {
            membersToDelete = dbMembers.stream()
                    .map(WecomSchoolDepartmentMember::getUserid)
                    .collect(Collectors.toSet());
        }
        
        // 解析企业微信成员数据
        List<WecomSchoolDepartmentMember> wechatMemberList = parseMembers(wechatMembers, departmentId);
        
        // 分类处理：新增和更新
        List<WecomSchoolDepartmentMember> membersToInsert = new ArrayList<>();
        List<WecomSchoolDepartmentMember> membersToUpdate = new ArrayList<>();
        
        for (WecomSchoolDepartmentMember wechatMember : wechatMemberList) {
            String userid = wechatMember.getUserid();
            
            // 从待删除列表中移除（因为这个成员在企业微信中存在）
            membersToDelete.remove(userid);
            
            // 判断是新增还是更新
            if (dbMemberMap.containsKey(userid)) {
                membersToUpdate.add(wechatMember);
            } else {
                membersToInsert.add(wechatMember);
            }
        }
        
        logger.info("部门 {} 成员同步 - 新增：{}, 更新：{}, 删除：{}", 
                departmentId, membersToInsert.size(), membersToUpdate.size(), membersToDelete.size());
        
        // 执行批量操作
        executeBatchOperations(membersToInsert, membersToUpdate, membersToDelete, departmentId);
    }

    /**
     * 解析企业微信成员数据
     * @param memberArray 成员数组
     * @param departmentId 部门 ID
     * @return 成员对象列表
     */
    private List<WecomSchoolDepartmentMember> parseMembers(JSONArray memberArray, Long departmentId) {
        // 创建成员对象列表
        List<WecomSchoolDepartmentMember> members = new ArrayList<>();
        // 如果成员数组为空，则返回空列表
        if (CollectionUtils.isEmpty(memberArray)) {
            return members;
        }
        // 遍历成员数组
        for (int i = 0; i < memberArray.size(); i++) {
            // 获取成员对象
            JSONObject memberObj = memberArray.getJSONObject(i);
            if (memberObj == null) {
                continue;
            }
            // 创建成员对象
            WecomSchoolDepartmentMember member = new WecomSchoolDepartmentMember();
            member.setUserid(memberObj.getString("userid"));
            member.setDepartmentId(departmentId);
            member.setName(memberObj.getString("name"));
            member.setOpenUserid(memberObj.getString("open_userid"));
            // 添加到列表中
            members.add(member);
        }
        // 返回成员列表
        return members;
    }

    /**
     * 执行批量操作（新增、更新、删除）
     * @param toInsert 待新增的成员列表
     * @param toUpdate 待更新的成员列表
     * @param toDelete 待删除的用户 ID 集合
     * @param departmentId 部门 ID
     */
    private void executeBatchOperations(List<WecomSchoolDepartmentMember> toInsert, 
                                        List<WecomSchoolDepartmentMember> toUpdate,
                                        Set<String> toDelete,
                                        Long departmentId) {
        // 执行批量新增
        if (!toInsert.isEmpty()) {
            wecomSchoolDepartmentMemberMapper.batchInsertSchoolDepartmentMembers(toInsert);
            logger.debug("部门 {} 批量新增成员 {} 个", departmentId, toInsert.size());
        }
        
        // 执行批量更新
        if (!toUpdate.isEmpty()) {
            for (WecomSchoolDepartmentMember member : toUpdate) {
                wecomSchoolDepartmentMemberMapper.updateSchoolDepartmentMember(member);
            }
            logger.debug("部门 {} 批量更新成员 {} 个", departmentId, toUpdate.size());
        }
        
        // 执行批量删除
        if (!toDelete.isEmpty()) {
            for (String userid : toDelete) {
                wecomSchoolDepartmentMemberMapper.deleteSchoolDepartmentMemberByUseridAndDepartmentId(userid, departmentId);
            }
            logger.debug("部门 {} 批量删除成员 {} 个", departmentId, toDelete.size());
        }
    }
}

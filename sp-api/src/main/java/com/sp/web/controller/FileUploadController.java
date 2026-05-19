package com.sp.web.controller;

import com.sp.common.annotation.Log;
import com.sp.common.core.controller.BaseController;
import com.sp.common.core.domain.AjaxResult;
import com.sp.common.enums.BusinessType;
import com.sp.common.utils.StringUtils;
import com.sp.common.utils.file.FileUploadUtils;
import com.sp.handler.FileUploadHandler;
import com.sp.system.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用文件上传处理
 */
@RestController
@RequestMapping("/common/upload")
public class FileUploadController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private FileUploadHandler fileUploadHandler;

    @Autowired
    private TokenService tokenService;

    /**
     * 通用文件上传
     * @param file 上传的文件
     * @param studentUserId 学生用户ID（可选，如果不传则使用默认学生）
     * @return 文件访问URL
     */
    @Log(title = "文件上传", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult uploadFile(@RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "studentUserId", required = false) String studentUserId,
                                  HttpServletRequest request) {
        try {
            // 从token获取家长ID
            String token = request.getHeader("Authorization");
            if (StringUtils.isNotEmpty(token) && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            String parentUserId = tokenService.getUserIdByToken(token);
            
            String filePath;
            String renamedFileName = null;
            
            if (StringUtils.isNotEmpty(parentUserId)) {
                // 构建自定义文件名，传入studentUserId
                renamedFileName = fileUploadHandler.buildCustomFileName(parentUserId, studentUserId);
                if (StringUtils.isNotEmpty(renamedFileName)) {
                    filePath = FileUploadUtils.uploadWithCustomName(file, renamedFileName);
                } else {
                    filePath = FileUploadUtils.uploadImage(file);
                }
            } else {
                logger.warn("无法从token获取家长ID，使用默认文件名上传");
                filePath = FileUploadUtils.uploadImage(file);
            }
            
            logger.info("文件上传成功: {}", filePath);
            
            // 返回文件访问URL和重命名后的文件名
            Map<String, Object> result = new HashMap<>();
            result.put("url", filePath);
            result.put("fileName", renamedFileName != null ? renamedFileName : file.getOriginalFilename());
            return AjaxResult.success("文件上传成功", result);
        } catch (Exception e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            return AjaxResult.error("文件上传失败: " + e.getMessage());
        }
    }
}

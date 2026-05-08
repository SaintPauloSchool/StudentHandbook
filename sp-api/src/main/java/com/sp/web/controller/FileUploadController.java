package com.sp.web.controller;

import com.sp.common.annotation.Log;
import com.sp.common.core.controller.BaseController;
import com.sp.common.core.domain.AjaxResult;
import com.sp.common.enums.BusinessType;
import com.sp.common.utils.file.FileUploadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 通用文件上传处理
 */
@RestController
@RequestMapping("/common/upload")
public class FileUploadController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    /**
     * 通用文件上传
     * @param file 上传的文件
     * @return 文件访问URL
     */
    @Log(title = "文件上传", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 上传文件并获取路径
            String filePath = FileUploadUtils.upload(file);
            
            logger.info("文件上传成功: {}", filePath);
            
            // 返回文件访问URL
            return AjaxResult.success("文件上传成功", filePath);
        } catch (Exception e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            return AjaxResult.error("文件上传失败: " + e.getMessage());
        }
    }
}

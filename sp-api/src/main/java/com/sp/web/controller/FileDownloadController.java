package com.sp.web.controller;

import com.sp.common.config.OverallSituationConfig;
import com.sp.common.constant.Constants;
import com.sp.common.utils.StringUtils;
import com.sp.common.utils.file.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 通用文件下載處理
 */
@RestController
@RequestMapping("/common/download")
public class FileDownloadController {

    private static final Logger log = LoggerFactory.getLogger(FileDownloadController.class);

    /**
     * 本地資源通用下載
     */
    @GetMapping("/resource")
    public void resourceDownload(String resource, String name, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (!FileUtils.checkAllowDownload(resource)) {
                response.sendError(HttpStatus.FORBIDDEN.value(), "資源文件非法，不允許下載");
                return;
            }
            String localPath = OverallSituationConfig.getProfile();
            String downloadPath = localPath + StringUtils.substringAfter(resource, Constants.RESOURCE_PREFIX);
            java.io.File file = new java.io.File(downloadPath);
            if (!file.exists() || !file.isFile()) {
                log.warn("下載文件不存在: {}", downloadPath);
                response.sendError(HttpStatus.NOT_FOUND.value(), "文件不存在");
                return;
            }
            String diskFileName = StringUtils.substringAfterLast(downloadPath, "/");
            String downloadName = diskFileName;
            if (StringUtils.isNotEmpty(name) && FileUtils.isValidFilename(name)) {
                downloadName = name;
            }
            String contentType = FileUtils.getContentTypeByFileName(downloadName);
            response.setContentType(contentType);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            FileUtils.writeBytes(downloadPath, response.getOutputStream());
        } catch (Exception e) {
            log.error("下載文件失敗", e);
            try {
                if (!response.isCommitted()) {
                    response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "下載失敗");
                }
            } catch (Exception ignored) {
                // ignore
            }
        }
    }
}

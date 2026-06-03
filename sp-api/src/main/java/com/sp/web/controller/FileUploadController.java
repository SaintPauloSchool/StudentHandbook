package com.sp.web.controller;

import com.sp.common.annotation.Log;
import com.sp.common.core.controller.BaseController;
import com.sp.common.core.domain.AjaxResult;
import com.sp.common.enums.BusinessType;
import com.sp.common.utils.StringUtils;
import com.sp.common.utils.file.FileUploadUtils;
import com.sp.handler.FileUploadHandler;
import com.sp.system.service.TokenService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用文件上傳處理（僅限圖片，最大 5MB）
 */
@RestController
@RequestMapping("/common/upload")
public class FileUploadController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    /** 允許的圖片副檔名 */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");

    /** 不支援的格式（HEIC/HEIF 給出專屬提示） */
    private static final List<String> UNSUPPORTED_HEIC = Arrays.asList("heic", "heif");

    /** 上傳大小上限：5MB */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;

    @Autowired
    private FileUploadHandler fileUploadHandler;

    @Autowired
    private TokenService tokenService;

    /**
     * 通用文件上傳
     * @param file 上傳的文件
     * @param studentUserId 學生用戶ID（可選，如果不傳則使用默認學生）
     * @return 文件訪問URL
     */
    @Log(title = "文件上傳", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult uploadFile(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "studentUserId", required = false) String studentUserId,
                                 HttpServletRequest request) {

        // ── 驗證：檔案不能為空 ──
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("請選擇要上傳的圖片");
        }

        // ── 驗證：副檔名格式 ──
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        if (StringUtils.isEmpty(ext)) {
            return AjaxResult.error("無法識別的文件格式，請上傳 JPG、PNG、GIF 或 BMP 圖片");
        }
        String extLower = ext.toLowerCase();

        if (UNSUPPORTED_HEIC.contains(extLower)) {
            return AjaxResult.error("不支援 HEIF/HEIC 格式。請在 iPhone「設定 → 相機 → 格式」中選擇「相容性最高」後重新拍照上傳");
        }

        if (!ALLOWED_EXTENSIONS.contains(extLower)) {
            return AjaxResult.error(String.format("不支援「.%s」格式，請上傳 JPG、PNG、GIF 或 BMP 圖片", ext));
        }

        // ── 驗證：文件大小（5MB）──
        if (file.getSize() > MAX_FILE_SIZE) {
            long sizeMB_x10 = file.getSize() * 10 / 1024 / 1024;
            String sizeMBStr = (sizeMB_x10 / 10) + "." + (sizeMB_x10 % 10);
            return AjaxResult.error(String.format("圖片大小 %sMB 超過限制，請上傳 5MB 以內的圖片", sizeMBStr));
        }

        try {
            // 從token獲取家長ID
            String token = request.getHeader("Authorization");
            if (StringUtils.isNotEmpty(token) && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            String parentUserId = tokenService.getUserIdByToken(token);

            String filePath;
            String renamedFileName = null;

            if (StringUtils.isNotEmpty(parentUserId)) {
                // 構建自定義文件名，傳入studentUserId
                renamedFileName = fileUploadHandler.buildCustomFileName(parentUserId, studentUserId);
                if (StringUtils.isNotEmpty(renamedFileName)) {
                    filePath = FileUploadUtils.uploadWithCustomName(file, renamedFileName);
                } else {
                    filePath = FileUploadUtils.uploadImage(file);
                }
            } else {
                logger.warn("無法從token獲取家長ID，使用默認文件名上傳");
                filePath = FileUploadUtils.uploadImage(file);
            }

            logger.info("文件上傳成功: {}", filePath);

            // 返回文件訪問URL和重命名後的文件名
            Map<String, Object> result = new HashMap<>();
            result.put("url", filePath);
            result.put("fileName", renamedFileName != null ? renamedFileName : file.getOriginalFilename());
            return AjaxResult.success("文件上傳成功", result);

        } catch (Exception e) {
            logger.error("文件上傳失敗: {}", e.getMessage(), e);
            return AjaxResult.error("文件上傳失敗：" + e.getMessage());
        }
    }
}

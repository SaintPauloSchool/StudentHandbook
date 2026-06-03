package com.sp.common.utils.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import com.sp.common.config.OverallSituationConfig;
import com.sp.common.constant.Constants;
import com.sp.common.exception.file.FileNameLengthLimitExceededException;
import com.sp.common.exception.file.FileSizeLimitExceededException;
import com.sp.common.exception.file.InvalidExtensionException;
import com.sp.common.utils.DateUtils;
import com.sp.common.utils.StringUtils;
import com.sp.common.utils.uuid.IdUtils;
import com.sp.common.utils.uuid.Seq;

/**
 * 文件上傳工具類
 *
 */
public class FileUploadUtils
{
    /**
     * 默認大小 5M
     */
    public static final long DEFAULT_MAX_SIZE = 5 * 1024 * 1024L;

    /**
     * 默認的文件名最大長度 100
     */
    public static final int DEFAULT_FILE_NAME_LENGTH = 100;

    /**
     * 默認上傳的地址
     */
    private static String defaultBaseDir = OverallSituationConfig.getProfile();

    public static void setDefaultBaseDir(String defaultBaseDir)
    {
        FileUploadUtils.defaultBaseDir = defaultBaseDir;
    }

    public static String getDefaultBaseDir()
    {
        return defaultBaseDir;
    }

    /**
     * 以默認配置進行文件上傳
     *
     * @param file 上傳的文件
     * @return 文件名稱
     * @throws Exception
     */
    public static final String upload(MultipartFile file) throws IOException
    {
        try
        {
            return upload(getDefaultBaseDir(), file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 僅允許圖片格式上傳（bmp, gif, jpg, jpeg, png）
     *
     * @param file 上傳的文件
     * @return 文件名稱
     * @throws Exception
     */
    public static final String uploadImage(MultipartFile file) throws IOException
    {
        try
        {
            return upload(getDefaultBaseDir(), file, MimeTypeUtils.IMAGE_EXTENSION);
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 使用自定義文件名上傳文件（僅允許圖片）
     *
     * @param file 上傳的文件
     * @param customFileName 自定義文件名（不含擴展名）
     * @return 文件名稱
     * @throws Exception
     */
    public static final String uploadWithCustomName(MultipartFile file, String customFileName) throws IOException
    {
        try
        {
            return uploadWithCustomName(getDefaultBaseDir(), file, customFileName, MimeTypeUtils.IMAGE_EXTENSION);
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 根據文件路徑上傳
     *
     * @param baseDir 相對應用的基目錄
     * @param file 上傳的文件
     * @return 文件名稱
     * @throws IOException
     */
    public static final String upload(String baseDir, MultipartFile file) throws IOException
    {
        try
        {
            return upload(baseDir, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 文件上傳
     *
     * @param baseDir 相對應用的基目錄
     * @param file 上傳的文件
     * @param allowedExtension 上傳文件類型
     * @return 返回上傳成功的文件名
     * @throws FileSizeLimitExceededException 如果超出最大大小
     * @throws FileNameLengthLimitExceededException 文件名太長
     * @throws IOException 比如讀寫文件出錯時
     * @throws InvalidExtensionException 文件校驗異常
     */
    public static final String upload(String baseDir, MultipartFile file, String[] allowedExtension)
            throws FileSizeLimitExceededException, IOException, FileNameLengthLimitExceededException,
            InvalidExtensionException
    {
        return upload(baseDir, file, allowedExtension, false);
    }

    /**
     * 文件上傳
     *
     * @param baseDir 相對應用的基目錄
     * @param file 上傳的文件
     * @param useCustomNaming 系統自定義文件名
     * @param allowedExtension 上傳文件類型
     * @return 返回上傳成功的文件名
     * @throws FileSizeLimitExceededException 如果超出最大大小
     * @throws FileNameLengthLimitExceededException 文件名太長
     * @throws IOException 比如讀寫文件出錯時
     * @throws InvalidExtensionException 文件校驗異常
     */
    public static final String upload(String baseDir, MultipartFile file, String[] allowedExtension, boolean useCustomNaming)
            throws FileSizeLimitExceededException, IOException, FileNameLengthLimitExceededException,
            InvalidExtensionException
    {
        int fileNameLength = Objects.requireNonNull(file.getOriginalFilename()).length();
        if (fileNameLength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH)
        {
            throw new FileNameLengthLimitExceededException(FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
        }

        assertAllowed(file, allowedExtension);

        String fileName = useCustomNaming ? uuidFilename(file) : extractFilename(file);

        String absPath = getAbsoluteFile(baseDir, fileName).getAbsolutePath();
        file.transferTo(Paths.get(absPath));
        return getPathFileName(baseDir, fileName);
    }

    /**
     * 編碼文件名(日期格式目錄 + 原文件名 + 序列值 + 後綴)
     */
    public static final String extractFilename(MultipartFile file)
    {
        return StringUtils.format("{}/{}_{}.{}", DateUtils.datePath(), FilenameUtils.getBaseName(file.getOriginalFilename()), Seq.getId(Seq.uploadSeqType), getExtension(file));
    }

    /**
     * 編編碼文件名(日期格式目錄 + UUID + 後綴)
     */
    public static final String uuidFilename(MultipartFile file)
    {
        return StringUtils.format("{}/{}.{}", DateUtils.datePath(), IdUtils.fastSimpleUUID(), getExtension(file));
    }

    /**
     * 使用自定義文件名上傳
     */
    public static final String uploadWithCustomName(String baseDir, MultipartFile file, String customFileName,
                                                    String[] allowedExtension)
            throws FileSizeLimitExceededException, IOException, FileNameLengthLimitExceededException,
            InvalidExtensionException
    {
        assertAllowed(file, allowedExtension);

        // 使用自定義文件名 + 日期目錄 + 擴展名
        String fileName = StringUtils.format("{}/{}.{}", DateUtils.datePath(), customFileName, getExtension(file));

        String absPath = getAbsoluteFile(baseDir, fileName).getAbsolutePath();
        file.transferTo(Paths.get(absPath));
        return getPathFileName(baseDir, fileName);
    }

    public static final File getAbsoluteFile(String uploadDir, String fileName) throws IOException
    {
        File desc = new File(uploadDir + File.separator + fileName);

        if (!desc.exists())
        {
            if (!desc.getParentFile().exists())
            {
                desc.getParentFile().mkdirs();
            }
        }
        return desc;
    }

    public static final String getPathFileName(String uploadDir, String fileName) throws IOException
    {
        int dirLastIndex = OverallSituationConfig.getProfile().length() + 1;
        String currentDir = StringUtils.substring(uploadDir, dirLastIndex);
        // 去除空格，防止路徑中包含隱藏的空格字符
        if (currentDir != null) {
            currentDir = currentDir.trim();
        }
        // 修復雙斜槓問題：如果 currentDir 爲空，直接返回 /profile/ + fileName
        if (StringUtils.isEmpty(currentDir)) {
            return Constants.RESOURCE_PREFIX + "/" + fileName;
        }
        return Constants.RESOURCE_PREFIX + "/" + currentDir + "/" + fileName;
    }

    /**
     * 文件大小校驗
     *
     * @param file 上傳的文件
     * @return
     * @throws FileSizeLimitExceededException 如果超出最大大小
     * @throws InvalidExtensionException
     */
    public static final void assertAllowed(MultipartFile file, String[] allowedExtension)
            throws FileSizeLimitExceededException, InvalidExtensionException
    {
        long size = file.getSize();
        if (size > DEFAULT_MAX_SIZE)
        {
            throw new FileSizeLimitExceededException(DEFAULT_MAX_SIZE / 1024 / 1024);
        }

        String fileName = file.getOriginalFilename();
        String extension = getExtension(file);
        if (allowedExtension != null && !isAllowedExtension(extension, allowedExtension))
        {
            if (allowedExtension == MimeTypeUtils.IMAGE_EXTENSION)
            {
                throw new InvalidExtensionException.InvalidImageExtensionException(allowedExtension, extension,
                        fileName);
            }
            else if (allowedExtension == MimeTypeUtils.FLASH_EXTENSION)
            {
                throw new InvalidExtensionException.InvalidFlashExtensionException(allowedExtension, extension,
                        fileName);
            }
            else if (allowedExtension == MimeTypeUtils.MEDIA_EXTENSION)
            {
                throw new InvalidExtensionException.InvalidMediaExtensionException(allowedExtension, extension,
                        fileName);
            }
            else if (allowedExtension == MimeTypeUtils.VIDEO_EXTENSION)
            {
                throw new InvalidExtensionException.InvalidVideoExtensionException(allowedExtension, extension,
                        fileName);
            }
            else
            {
                throw new InvalidExtensionException(allowedExtension, extension, fileName);
            }
        }
    }

    /**
     * 判斷MIME類型是否是允許的MIME類型
     *
     * @param extension
     * @param allowedExtension
     * @return
     */
    public static final boolean isAllowedExtension(String extension, String[] allowedExtension)
    {
        for (String str : allowedExtension)
        {
            if (str.equalsIgnoreCase(extension))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 獲取文件名的後綴
     *
     * @param file 表單文件
     * @return 後綴名
     */
    public static final String getExtension(MultipartFile file)
    {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (StringUtils.isEmpty(extension))
        {
            extension = MimeTypeUtils.getExtension(Objects.requireNonNull(file.getContentType()));
        }
        return extension;
    }
}
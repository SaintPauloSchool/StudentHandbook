package com.sp.system.service.impl;

import com.sp.system.entity.vo.StudentPhotoVO;
import com.sp.system.service.IStudentPhotoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * 學生照片服務實現
 * <p>
 * 按配置的 url-template 代理拉取外部學生照片，供前端頭像展示。
 */
@Service
public class StudentPhotoServiceImpl implements IStudentPhotoService {

    private static final Logger log = LoggerFactory.getLogger(StudentPhotoServiceImpl.class);

    private static final Pattern PROFILE_NUMBER_PATTERN = Pattern.compile("^[0-9]{1,20}$");

    private static final String PROFILE_NUMBER_PLACEHOLDER = "{studentProfileNumber}";

    @Value("${student-info.photo.url-template:}")
    private String photoUrlTemplate;

    @Value("${student-info.photo.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${student-info.photo.read-timeout-ms:10000}")
    private int readTimeoutMs;

    /**
     * 根據學籍號拉取學生照片二進制數據
     *
     * @param studentProfileNumber 學籍號（student_profile_number）
     * @return 照片數據；配置缺失、號碼非法或遠端無圖時返回 null
     */
    @Override
    public StudentPhotoVO fetchPhoto(String studentProfileNumber) {
        if (!isValidProfileNumber(studentProfileNumber)) {
            return null;
        }
        if (!StringUtils.hasText(photoUrlTemplate) || !photoUrlTemplate.contains(PROFILE_NUMBER_PLACEHOLDER)) {
            log.warn("學生照片 url-template 未配置或缺少 {} 占位符", PROFILE_NUMBER_PLACEHOLDER);
            return null;
        }

        String photoUrl = photoUrlTemplate.replace(PROFILE_NUMBER_PLACEHOLDER, studentProfileNumber.trim());

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(photoUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(connectTimeoutMs);
            connection.setReadTimeout(readTimeoutMs);
            connection.setRequestProperty("Accept", "image/*");
            connection.connect();

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                log.debug("學生照片不存在或請求失敗，profileNumber={}, status={}", studentProfileNumber, status);
                return null;
            }

            String contentType = connection.getContentType();
            if (!StringUtils.hasText(contentType) || !contentType.toLowerCase().startsWith("image/")) {
                contentType = MediaType.IMAGE_JPEG_VALUE;
            }

            byte[] data = readBytes(connection.getInputStream());
            if (data.length == 0) {
                return null;
            }
            return new StudentPhotoVO(data, contentType);
        } catch (Exception e) {
            log.warn("拉取學生照片失敗，profileNumber={}", studentProfileNumber, e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private boolean isValidProfileNumber(String studentProfileNumber) {
        return StringUtils.hasText(studentProfileNumber)
                && PROFILE_NUMBER_PATTERN.matcher(studentProfileNumber.trim()).matches();
    }

    private byte[] readBytes(InputStream inputStream) throws Exception {
        try (InputStream in = inputStream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
        }
    }
}

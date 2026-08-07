package com.sp.system.entity.vo;

/**
 * 學生照片代理結果
 */
public class StudentPhotoVO {

    private final byte[] data;
    private final String contentType;

    public StudentPhotoVO(byte[] data, String contentType) {
        this.data = data;
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }
}

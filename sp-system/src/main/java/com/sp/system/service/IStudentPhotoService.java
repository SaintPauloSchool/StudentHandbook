package com.sp.system.service;

import com.sp.system.entity.vo.StudentPhotoVO;

/**
 * 學生照片代理服務
 */
public interface IStudentPhotoService {

    StudentPhotoVO fetchPhoto(String studentProfileNumber);
}

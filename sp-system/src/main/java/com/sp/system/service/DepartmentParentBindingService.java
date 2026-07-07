package com.sp.system.service;

/**
 * 部門家長綁定服務層
 */
public interface DepartmentParentBindingService {

    /**
     * 檢查家長是否綁定了學生（即綁定了學生用戶ID的記錄）
     * @param parentUserId 家長用戶ID
     * @return 是否有綁定
     */
    boolean checkHasBoundStudents(String parentUserId);
}

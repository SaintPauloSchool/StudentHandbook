# 學生手冊系統 (Student Handbook System)

## 項目概述

學生手冊系統是一個基於企業微信家校應用的全棧解決方案，用於幫助家長查看孩子的功課及測驗安排。該系統通過與企業微信集成，實現了家長、學生之間的高效溝通。

## 技術架構

### 後端技術棧
- **框架**: Spring Boot 2.5.15
- **數據庫**: MySQL
- **ORM框架**: MyBatis Plus
- **緩存**: Ehcache
- **數據庫連接池**: Druid
- **API文檔**: Swagger 3.0
- **前端框架**: Vue 3 + Element Plus
- **構建工具**: Maven

### 項目模塊結構

```
StudentHandbook/
├── sp-api/                 # Spring Boot 主應用
├── sp-common/              # 通用工具和配置
├── sp-framework/           # 框架層
├── sp-system/              # 業務邏輯層
├── student-handbook-vue/   # Vue 3 前端應用
├── sql/                    # 數據庫腳本
└── bin/                    # 腳本文件
```

## 功能特性

### 1. 課程日誌管理
- 支持查看功課和測驗安排
- 提供今日、過去一個月、未來七天視圖
- 自動過濾僅顯示'功課'和'測驗'類型的課程日誌

### 2. 家長學生關係管理
- 通過企業微信同步家長學生關係
- 支持多個學生綁定同一個家長賬戶
- 提供學生切換功能

### 3. 部門管理
- 支持班級、年級、學段等多級結構
- 與企業微信部門數據同步

### 4. 企業微信集成
- 使用企業微信家校應用接口
- 支持OAuth2授權登錄
- 自動同步企業微信用戶數據

### 5. 響應式設計
- 支持桌面和移動設備訪問
- 滑動手勢支持
- 自適應界面佈局

## 數據庫設計

### 主要表結構

1. **class_log** - 課程日誌表
   - 存儲功課、測驗等課程安排
   - 包含班級、教師、課程、內容等信息

2. **sys_department** - 部門表
   - 管理班級、年級、學段等組織結構
   - 支持樹形結構

3. **sys_parent_student_relation** - 家長學生關係表
   - 維護家長與學生的綁定關係
   - 包含關係描述、手機號等信息

4. **sys_department_parent_binding** - 部門家長綁定表
   - 綁定家長到特定部門(班級)

5. **sys_token** - Token表
   - 管理用戶認證令牌

6. **class_section** - 課程班級表
   - 映射企業微信班級代碼到內部班級代碼

## 部署指南

### 環境要求
- Java 8+
- Maven 3.5+
- Node.js 14+
- MySQL 5.7+

### 部署步驟 (前後端完全分離)

本系統目前採用前後端完全分離部署架構。

1. **前端打包與部署 (Nginx 代理)**
   ```bash
   cd student-handbook-vue
   npm install
   
   # 打包生產環境文件
   npm run build
   ```
   **發佈說明**：將生成的 `dist` 文件夾上傳至服務器的靜態目錄（如 `/usr/share/nginx/tals-vue/dist`）。並在服務器 Nginx 中的 `location /` 塊指向該目錄，注意添加 `try_files $uri $uri/ /index.html;` 以支持 Vue History 路由。

2. **後端打包與部署 (Tomcat / API 服務)**
   ```bash
   # 配置數據庫等信息
   # 按需修改 sp-api/src/main/resources/application-*.yml
   
   # 編譯打包 (純 API 包，不包含前端代碼)
   mvn clean package
   ```
   **發佈說明**：將生成的 `.war` (或 `.jar`) 部署到服務器的 Tomcat 中啟動。在服務器的 Nginx 配置中，通過 `location /sp-api/` 規則，將所有的數據請求 `proxy_pass` 反向代理至 Tomcat 的 `8003` 端口。

### 配置及架構說明
- **前端部署位置**: Nginx 根路徑 `/` (直接由 Nginx 高效處理 HTML、JS、CSS 等靜態資源)
- **後端 API 端口**: `8003` (Java Tomcat 負責運算和數據)
- **API 代理路徑**: `/sp-api/` (所有請求後端的接口調用、微信回調均會被 Nginx 攔截並轉發至後端)

## API 接口

主要接口包括：
- `/system/handbook/list` - 獲取手冊列表
- `/system/handbook/today` - 獲取今日安排
- `/system/handbook/pastMonth` - 獲取過去一個月安排
- `/system/handbook/nextSevenDays` - 獲取未來七天安排
- `/system/handbook/students` - 獲取學生列表
- `/system/handbook/switchStudent` - 切換學生

## 安全特性

- Token認證機制
- XSS攻擊防護
- 數據脫敏處理
- 接口訪問權限控制

## 定時任務

本系統包含以下定時任務，用於自動同步企業微信數據和發送通知：

### 1. 家長學生部門數據同步任務 (DepartmentSyncTask)
- **執行時間**: 每天凌晨 0:00 執行
- **Cron 表達式**: `0 0 0 * * ?`
- **功能描述**:
  - 從企業微信獲取部門列表
  - 同步部門信息到本地數據庫（sys_department 表）
  - 支持新增和更新操作
- **防重複執行**: 使用 AtomicBoolean 確保同一時間只有一個實例在執行

### 2. 家長學生關係同步任務 (ParentStudentRelationSyncTask)
- **執行時間**: 每天凌晨 0:30 執行
- **Cron 表達式**: `0 30 0 * * ?`
- **功能描述**:
    - 獲取所有班級部門 ID 列表
    - 遍歷每個部門，從企業微信獲取家長列表
    - 處理家長的孩子信息，創建或更新家長學生關係
    - 保存到 sys_parent_student_relation 和 sys_department_parent_binding 表
    - 自動刪除不再存在的家長綁定記錄
- **防重複執行**: 使用 AtomicBoolean 確保同一時間只有一個實例在執行

### 3. 學校部門列表與成員同步任務 (DepartmentSimpleListTask)
- **執行時間**: 每天凌晨 1:00 執行
- **Cron 表達式**: `0 0 1 * * ?`
- **功能描述**:
  - 調用企業微信接口獲取所有部門數據
  - 同步部門信息到本地數據庫（以企業微信數據為準進行增刪改）
  - 根據部門 ID 列表，同步所有成員數據到本地數據庫
  - 支持部門和成員的全量更新
- **防重複執行**: 使用 AtomicBoolean 確保同一時間只有一個實例在執行

### 4. 課程日誌數據同步任務 (ClassLogSyncTask)（目前註銷狀態）
- **執行時間**: 每週一至週五下午 17:50 執行（目前註銷狀態）
- **Cron 表達式**: `0 50 17 ? * MON-FRI` (已註銷)
- **功能描述**:
  - 從外部數據庫獲取課程日誌數據
  - 批量同步到本地數據庫（class_log 表）
  - 使用 upsert 方式（存在則更新，不存在則插入）
- **防重複執行**: 使用 AtomicBoolean 確保同一時間只有一個實例在執行

### 5. 學校通知發送任務 (SchoolNoticeTask)
- **執行時間**: 每週一至週五下午 18:00 執行
- **Cron 表達式**: `0 0 18 ? * MON-FRI`
- **功能描述**:
  - 從 sys_department_parent_binding 表獲取所有家長用戶 ID
  - 通過企業微信家校應用接口發送通知
  - 通知內容包含學生手冊訪問鏈接
- **防重複執行**: 使用 AtomicBoolean 確保同一時間只有一個實例在執行

### 定時任務配置說明

1. **任務執行保障**:
   - 所有定時任務都使用 `AtomicBoolean` 實現防重複執行機制
   - 如果任務正在執行，會跳過本次調度並記錄日誌

2. **錯誤處理**:
   - 所有任務都包含完整的 try-catch-finally 異常處理
   - 執行完成後會釋放鎖，確保下次調度正常執行
   - 詳細的日誌記錄便于問題排查

3. **時區設置**:
   - 使用 `Asia/Shanghai` 時區（北京時間）
   - 確保定時任務按照預期時間執行

4. **依賴服務**:
   - 所有任務依賴企業微信 API 接口
   - 需要正確配置企業微信應用的相關參數
   - 需要保證網絡連通性

## 開發指南

### 後端開發
- 使用Spring Boot進行業務邏輯開發
- MyBatis進行數據庫操作
- 遵循RESTful API設計規範

### 前端開發
- 使用Vue 3 + Composition API
- Element Plus組件庫
- Axios進行API調用
- Vite作為構建工具

### 開發測試說明
- 如果是dev開發時，請在[application.yml](sp-api/src/main/resources/application.yml)文件加上parentUserId數據測試，以便於調試家長學生關係相關功能

## 維護注意事項

1. 定期同步企業微信數據
2. 監控課程日誌數據傳輸任務
3. 保持企業微信配置的安全性
4. 定期備份數據庫

## 前端認證開關配置

本項目提供了兩個全局開關，用於控制前端的認證行為：

1. 微信驗證開關：控制是否啟用微信授權登錄
2. Token驗證開關：控制是否啟用Token驗證

### 配置文件

配置文件位於 `student-handbook-vue/src/config/settings.js`，包含以下配置項：

```javascript
export default {
  // 微信驗證開關
  enableWeChatAuth: true,
  
  // Token驗證開關
  enableTokenAuth: true,
  
  // 登錄頁面路徑
  loginPath: '/login'
}
```

### 功能說明

#### 微信驗證開關 (enableWeChatAuth)
- 當設置為 `true` 時：
  - 啟用微信相關功能
  - 在微信環境中自動加載微信JS-SDK
  - 執行微信授權流程
  - 在登錄頁面自動觸發微信登錄

- 當設置為 `false` 時：
  - 禁用微信相關功能
  - 不加載微信JS-SDK
  - 跳過微信授權流程
  - 直接跳轉到首頁

#### Token驗證開關 (enableTokenAuth)
- 當設置為 `true` 時：
  - 啟用Token驗證
  - 在請求頭中添加Authorization字段
  - 攔截401/403錯誤並跳轉到登錄頁
  - 在路由守衛中檢查Token有效性

- 當設置為 `false` 時：
  - 禁用Token驗證
  - 不在請求頭中添加Authorization字段
  - 不攔截401/403錯誤
  - 路由守衛允許所有訪問

### 使用場景

#### 開發環境
在開發環境中，可以將這兩個開關設為 `false`，以便於調試和測試，無需每次都要經過認證流程。

#### 測試環境
在測試環境中，可以根據需要啟用或禁用特定的驗證方式，以測試不同的業務邏輯。

#### 生產環境
在生產環境中，建議保持兩個開關都為 `true`，以確保安全性。

### 修改配置
要修改開關設置，只需編輯 `student-handbook-vue/src/config/settings.js` 文件中的對應值即可。

### 注意事項
1. 當禁用Token驗證時，所有API請求將不會攜帶Token信息
2. 當禁用微信驗證時，用戶無法通過微信進行登錄
3. 修改配置後需要重新構建和部署前端應用才能生效
4. 在StudentHandbook.vue組件中，當禁用Token驗證時，切換學生功能將不再檢查token的存在性

## 貢獻

如需貢獻代碼或報告問題，請提交Issue或Pull Request。

## 版權

© 2025 學生手冊系統. 版權聖保祿學校所有.
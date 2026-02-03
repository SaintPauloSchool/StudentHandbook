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

### 部署步驟

1. **前端部署**
   ```bash
   cd student-handbook-vue
   npm install
   npm run build
   
   或
   
   npm run dev 对应 development 模式
   npm run build 对应 production 模式
   npm run build:test 对应 test 模式
   
   # 前端資源會自動複製到後端靜態資源目錄
   ```

2. **後端部署**
   ```bash
   # 配置數據庫連接
   # 修改 sp-api/src/main/resources/application-*.yml
   
   #回到根目錄
   cd ..
   
   # 編譯打包
   mvn clean compile
   
   mvn clean package
   ```

3. **發佈服務器**
   ```bash

   ```

### 配置說明
- 默認服務器端口: 8003
- 訪問路徑: `/sp-api`
- 静態資源路徑: `/dist/**`

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

- 部門數據同步 - 每天凌晨12點執行
- 家長學生關係同步 - 定期從企業微信同步數據
- 課程日誌數據傳輸 - 從源數據庫同步到目標數據庫

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
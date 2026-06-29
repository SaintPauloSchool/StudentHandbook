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

- Java 8+（服務器建議 Java 17）
- Maven 3.5+
- Node.js 16+
- MySQL 5.7+
- Nginx 1.18+

### 架構概覽

```
用戶（微信 / 瀏覽器）
        │
        ▼
   Nginx（固定配置，發版一般不改）
   ├── /              → 學生手冊 Vue 靜態資源（dist）
   ├── /sp-api/       → 後端 API（Tomcat / Spring Boot）
   ├── /profile/      → 上傳文件
   └── /school-management-system/  → 校園系統（另一套前端）
```

**核心原則：前後端獨立發版，互不干擾。**

| 組件 | 發版頻率 | 發版時是否改 Nginx |
|------|----------|-------------------|
| 前端 `student-handbook-vue` | 有 UI/邏輯變更時 | 否 |
| 後端 `sp-api` | 有 API/業務變更時 | 否 |
| Nginx | 僅首次部署或調整路由/緩存時 | — |

**緩存策略（全部由 Nginx + Vite 文件名 hash 處理，無需 URL 版本號）：**

- `index.html` → 禁止緩存，每次訪問拉最新入口
- `/assets/*-[hash].js|css|...` → 長期緩存（hash 變了 URL 自然變，舊緩存不影響）

---

### 環境對照

| 環境 | 訪問地址 | 前端靜態目錄 | 後端端口 | Spring Profile |
|------|----------|--------------|----------|----------------|
| 本地開發 | `http://localhost:3000` | Vite dev server | `8002` | `dev` |
| 測試服 | `http://10.32.96.55:8082` | `/usr/share/nginx/student-handbook/dev/dist` | `8002` | `test` |
| 生產 | `http://tals-wcapp.esp.edu.mo` | `/usr/share/nginx/student-handbook/prod/dist` | `8003` | `prod` |

後端 OAuth 登錄完成後會 redirect 到 `sp.frontend.url`（見各環境 `application-*.yml`），請確保與實際訪問地址一致。

---

### 一、首次部署（服務器初始化，只需做一次）

#### 1. 創建目錄

```bash
sudo mkdir -p /usr/share/nginx/student-handbook/prod/dist
sudo mkdir -p /usr/share/nginx/student-handbook/dev/dist
```

#### 2. 配置 Nginx

完整示例見：`student-handbook-vue/deploy/nginx-full-example.conf`

將其中學生手冊相關的 `server` 塊合併進服務器現有 Nginx 配置（注意保留已有的 `/sp-api/`、`/school-management-system/` 等規則）。

```bash
sudo nginx -t
sudo systemctl reload nginx
```

#### 3. 部署後端

```bash
# 在開發機打包
mvn clean package -DskipTests

# 上傳 sp-api/target/*.war 到服務器 Tomcat
# 生產使用 application-prod.yml（端口 8003）
# 測試使用 application-test.yml（端口 8002）
```

#### 4. 部署前端

```bash
cd student-handbook-vue
npm install
npm run build
# 將 dist/ 上傳到對應目錄（見上方環境對照表）
```

#### 5. 驗證

```bash
# 首頁可訪問
curl -sI http://tals-wcapp.esp.edu.mo/ | head -5

# API 可達
curl -sI http://tals-wcapp.esp.edu.mo/sp-api/ | head -5

# index.html 應帶 no-cache
curl -sI http://tals-wcapp.esp.edu.mo/index.html | grep -i cache
```

---

### 二、日常發版：僅前端有變更

```bash
cd student-handbook-vue
npm install          # 依賴有變時才需要
npm run build        # 產出 dist/
```

**上傳覆蓋服務器目錄：**

```bash
# 生產示例（按實際登錄方式調整 scp/rsync）
rsync -avz --delete dist/ user@server:/usr/share/nginx/student-handbook/prod/dist/
```

完成。無需 reload Nginx，無需重啟後端。

**驗證：** 微信或瀏覽器打開首頁，強制刷新；或查看 `index.html` 引用的 `assets/index-xxxxx.js` 文件名是否已變。

---

### 三、日常發版：僅後端有變更

```bash
# 在項目根目錄
mvn clean package -DskipTests
```

1. 停止 Tomcat 中舊的 `sp-api` 應用
2. 上傳新 war 並啟動
3. 確認 `application-prod.yml` / `application-test.yml` 中數據庫、企業微信等配置正確

完成。無需重新 build 前端，無需改 Nginx。

**驗證：**

```bash
curl -s http://tals-wcapp.esp.edu.mo/sp-api/system/token/validate \
  -H "Authorization: Bearer <有效token>"
```

---

### 四、本地開發

**後端：**

```bash
# IDE 或命令行啟動 sp-api，profile = dev，端口 8002
# 配置見 sp-api/src/main/resources/application-dev.yml
# sp.frontend.url 默認 http://localhost:3000/
```

**前端：**

```bash
cd student-handbook-vue
npm install
npm run dev
# 訪問 http://localhost:3000
# Vite 已配置 /sp-api → localhost:8002 代理
```

非生產環境下，瀏覽器打開 `/login` 會自動走 `code=dev` 模擬登錄，無需微信。

---

### 五、微信登錄與瀏覽器使用

1. 在企業微信 / 微信內打開 `http://tals-wcapp.esp.edu.mo/`
2. 完成 OAuth，後端 redirect 到 `/?token=xxx`
3. 在微信內瀏覽時，URL 會保留 token，可用微信自帶「複製鏈接」
4. 粘貼到 Chrome / Safari 即可在瀏覽器內操作

後端回跳地址配置：`sp.frontend.url`（`application-prod.yml` 等）。

---

### 六、回滾

| 場景 | 做法 |
|------|------|
| 前端回滾 | 用上一版 `dist/` 備份覆蓋服務器目錄 |
| 後端回滾 | 部署上一版 war |
| Nginx 回滾 | 恢復上一版配置文件後 `nginx -t && reload` |

建議每次前端發版前備份：`cp -r dist dist.bak.$(date +%Y%m%d)`。

---

### 七、常見問題

**Q：發版後微信還是舊頁面？**  
A：多為微信 WebView 緩存。讓用戶完全退出頁面重新從應用入口打開；`index.html` 已設 no-cache，新入口會拉新 JS。

**Q：需要每次發版改 Nginx 嗎？**  
A：不需要。除非新增路由前綴（如新子系統）或調整緩存策略。

**Q：舊版帶 `/20260623.../` 時間戳前綴的書籤還能用嗎？**  
A：已廢棄該方案。請從微信應用入口或根路徑 `http://tals-wcapp.esp.edu.mo/` 重新進入。

---

### 配置要點速查

- **前端 API 前綴**：`/sp-api`（`src/config/api.js`）
- **Nginx 配置模板**：`student-handbook-vue/deploy/nginx-full-example.conf`
- **前端認證開關**：`student-handbook-vue/src/config/settings.js`
- **後端前端回跳**：`sp.frontend.url` in `application-prod.yml` / `application-test.yml` / `application-dev.yml`
- **企業微信 OAuth 回調**：`https://mo-stu-sys.org-assistant.com/sp-api/wechat/oauth/callback`（生產）

---

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
   - 詳細的日誌記錄便於問題排查

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
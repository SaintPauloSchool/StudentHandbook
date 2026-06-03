<template>
  <div class="home-container" v-loading.fullscreen.lock="isNavigatingToCampus" element-loading-text="正在驗證並跳轉至校園系統...">

    <!-- 頂部學生資訊欄：固定在頁面最上方，左姓名右切換 -->
    <div class="student-top-bar" v-if="(userType === 0 || userType === 1) && currentStudentName">
      <div class="student-name-area">
        <el-icon class="student-avatar-icon"><User /></el-icon>
        <span class="student-name-text">{{ currentStudentName }}</span>
      </div>
      <button class="switch-student-btn" @click="openStudentSwitchDialog">切換學生</button>
    </div>

    <div class="welcome-section">
      <div class="logo-badge">
        <img src="../logo/sp.jpg" alt="School Logo" class="school-logo-img">
      </div>
      <h1 class="welcome-title">歡迎使用學生系統</h1>
    </div>

    <div class="buttons-container">
      <div class="button-wrapper" v-if="userType === 0 || userType === 1 || userType === null">
        <button class="feature-button primary-button" @click="goToStudentHandbook">
          <div class="button-content">
            <span class="button-icon">📘</span>
            <span class="button-text">學生手冊</span>
          </div>
        </button>
      </div>

      <div class="button-wrapper" v-if="userType === 0 || userType === 1 || userType === null">
        <button class="feature-button success-button" @click="goToParentNotice">
          <div class="button-content">
            <span class="button-icon">📢</span>
            <span class="button-text">家校通知</span>
          </div>
        </button>
        <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
      </div>

      <div class="button-wrapper" v-if="userType === 0 || userType === 1 || userType === null">
        <button class="feature-button info-button" @click="goToCalendar">
          <div class="button-content">
            <span class="button-icon">📅</span>
            <span class="button-text">行事曆</span>
          </div>
        </button>
      </div>

      <div class="button-wrapper" v-if="userType === 2 || userType === null">
        <button class="feature-button warning-button" @click="goToCampusSystem">
          <div class="button-content">
            <span class="button-icon">🏫</span>
            <span class="button-text">校園系統</span>
          </div>
        </button>
      </div>
    </div>

    <!-- 共用切換學生彈窗元件 -->
    <StudentSwitchDialog v-model="studentDialogVisible" @switched="onStudentSwitched" />
  </div>
</template>

<script>
import service from '@/utils/request.js'
import {ElMessage} from 'element-plus'
import settings from '@/config/settings'
import { API_ENDPOINTS, baseURL } from '@/config/api.js'
import StudentSwitchDialog from '@/components/StudentSwitchDialog.vue'
import { User } from '@element-plus/icons-vue'

export default {
  name: 'Home',
  components: { StudentSwitchDialog, User },
  data() {
    const cachedUserType = localStorage.getItem('userType');
    return {
      unreadCount: 0,
      isNavigatingToCampus: false,
      userType: cachedUserType !== null ? parseInt(cachedUserType) : null,
      currentStudentName: localStorage.getItem('currentStudentName') || '', // 當前學生姓名
      studentDialogVisible: false // 切換彈窗是否顯示
    }
  },
  async mounted() {
    // 檢查URL參數中是否有token（來自微信授權回調）
    this.checkTokenFromUrl();

    // 根據配置決定是否執行Token驗證
    if (settings.enableTokenAuth) {
      // 檢查是否存在token，如果沒有則重定向到登錄頁面
      this.checkToken();
    }

    // ① 先確保默認學生 ID 已存入 localStorage（首次登入時 localStorage 中尚無記錄）
    await this.ensureDefaultStudent();

    // ② 有了正確的 studentUserId 後，再拉未讀數，確保數字對應正確的學生
    this.fetchUnreadCount();

    // 獲取使用者資訊（包含 userType）
    this.fetchUserInfo();

    // 監聽學生切換事件
    window.addEventListener('studentChanged', this.handleStudentChanged);
  },
  beforeUnmount() {
    // 移除事件監聽器
    window.removeEventListener('studentChanged', this.handleStudentChanged);
  },

  methods: {
    // 檢查URL參數中的token和userType
    checkTokenFromUrl() {
      const urlParams = new URLSearchParams(window.location.search);
      const token = urlParams.get('token');
      const urlUserType = urlParams.get('userType');
      const state = urlParams.get('state');

      if (token) {
        // 保存token到本地存儲，同時記錄過期時間 (7天)
        localStorage.setItem('token', token);
        localStorage.setItem('token_expire', (Date.now() + 7 * 24 * 60 * 60 * 1000).toString());

        // 如果URL中有傳遞 userType，直接保存並使用，避免按鈕閃爍
        if (urlUserType !== null) {
          this.userType = parseInt(urlUserType);
          localStorage.setItem('userType', this.userType);
        } else {
          localStorage.removeItem('userType'); // 登入時清除舊的 userType
          this.userType = null;
        }

        // 清除URL中的參數，避免在地址欄顯示敏感信息
        urlParams.delete('token');
        urlParams.delete('userType');
        if (state) urlParams.delete('state');
        const newUrl = window.location.pathname +
            (urlParams.toString() ? '?' + urlParams.toString() : '') +
            window.location.hash;
        window.history.replaceState({}, document.title, newUrl);

        // 若之前是因為 token 過期後重新授權，授權完成後自動打開校園系統
        if (sessionStorage.getItem('pendingCampusRedirect') === 'true' || (state && state.startsWith('campus_notice_'))) {
          sessionStorage.removeItem('pendingCampusRedirect');

          let targetUrl = settings.campusSystemUrl;
          if (state && state.startsWith('campus_notice_')) {
            const noticeId = state.replace('campus_notice_', '');
            if (noticeId !== 'root') {
              // 確保 URL 結尾有斜線或者處理拼接
              const baseUrl = settings.campusSystemUrl.endsWith('/') ? settings.campusSystemUrl : settings.campusSystemUrl + '/';
              targetUrl = `${baseUrl}${noticeId}`;
            }
          }

          const campusUrl = `${targetUrl}${targetUrl.includes('?') ? '&' : '?'}token=${encodeURIComponent(token)}`;
          console.log('重新授權完成，自動跳轉到校園系統:', campusUrl);

          // 如果是直接點擊通知進來的，直接替換當前頁面；否則是原本的打開新分頁
          if (state && state.startsWith('campus_notice_')) {
            window.location.replace(campusUrl);
          } else {
            window.open(campusUrl, '_blank');
          }
        } else {
          ElMessage.success('登錄成功');
        }
      }
    },

    // ① 首次登入時，確保默認學生已嵌入 localStorage，並同步顯示姓名
    async ensureDefaultStudent() {
      // 如果已經有緬存的學生 ID，直接同步姓名後返回
      if (localStorage.getItem('currentStudentUserId')) {
        this.currentStudentName = localStorage.getItem('currentStudentName') || '';
        this.selectedStudentUserId = localStorage.getItem('currentStudentUserId') || '';
        // 若只有 ID 沒有姓名，仍需拉一次列表補全
        if (this.currentStudentName) return;
      }

      try {
        const response = await service.get(API_ENDPOINTS.STUDENT_HANDBOOK_STUDENTS);
        if (response.data.code === 200) {
          const relations = response.data.data;
          if (relations && relations.length > 0) {
            this.studentRelations = relations;
            const savedId = localStorage.getItem('currentStudentUserId');
            const matched = savedId ? relations.find(r => r.studentUserId === savedId) : null;
            const defaultRel = matched || relations[0];
            // 設定默認學生
            this.currentStudentName = defaultRel.studentName;
            this.selectedStudentUserId = defaultRel.studentUserId;
            localStorage.setItem('currentStudentUserId', defaultRel.studentUserId);
            localStorage.setItem('currentStudentName', defaultRel.studentName);
            console.log('首頁初始化：設置默認學生', defaultRel.studentName);
          }
        }
      } catch (error) {
        console.warn('首頁初始化：無法取得學生列表（可能是員工身份）', error.message);
      }
    },

    // 檢查是否存在token
    checkToken() {
      const token = localStorage.getItem('token');
      if (!token) {
        // 如果沒有token，重定向到登錄頁面
        this.$router.push('/login');
      }
    },

    // 獲取未讀通知數量
    async fetchUnreadCount() {
      try {
        // 從localStorage獲取當前選中的學生ID
        const studentUserId = localStorage.getItem('currentStudentUserId');

        const params = {};
        if (studentUserId) {
          params.studentUserId = studentUserId;
        }

        const response = await service.get(API_ENDPOINTS.NOTICE_UNREAD_COUNT, {
          params: params
        });
        const res = response.data;
        if (res.code === 200 && res.data) {
          this.unreadCount = res.data.unreadCount || 0;
          console.log('設置未讀數量爲:', this.unreadCount); // 調試信息
        }
      } catch (error) {
        console.error('獲取未讀通知數量失敗:', error);
      }
    },

    // 獲取使用者資訊
    async fetchUserInfo() {
      const token = localStorage.getItem('token');
      if (!token) return;

      try {
        const response = await service.get(API_ENDPOINTS.VALIDATE_TOKEN);
        if (response.data.code === 200 && response.data.data) {
          this.userType = response.data.data.userType;
          localStorage.setItem('userType', this.userType);
        }
      } catch (error) {
        console.error('獲取使用者資訊失敗:', error);
      }
    },

    goToStudentHandbook() {
      // 跳轉到學生手冊頁面
      this.$router.push('/handbook');
    },
    goToParentNotice() {
      // 跳轉到家校通知頁面
      this.$router.push('/notice');
    },
    goToCalendar() {
      // 跳轉到行事曆頁面
      this.$router.push('/calendar');
    },
    async goToCampusSystem() {
      // 跳轉到校園系統（在新分頁開啟），並帶上 token
      const token = localStorage.getItem('token');

      if (!token) {
        // 本地沒有 token，直接走微信重新授權
        this.reAuthAndOpenCampus();
        return;
      }

      this.isNavigatingToCampus = true;
      try {
        // 呼叫後端驗證 token 是否在資料庫中真的有效（防止被手動改過期或被撤銷）
        const response = await service.get(API_ENDPOINTS.VALIDATE_TOKEN);
        if (response.data.code === 200) {
          // Token 確實有效，直接開新分頁跳轉
          const url = `${settings.campusSystemUrl}?token=${encodeURIComponent(token)}`;
          window.open(url, '_blank');
        } else {
          // Token 無效，重新授權
          this.reAuthAndOpenCampus();
        }
      } catch (error) {
        console.error('驗證 Token 發生錯誤:', error);
        // 如果 API 報錯（例如網絡問題），為了安全起見，重新授權
        this.reAuthAndOpenCampus();
      } finally {
        this.isNavigatingToCampus = false;
      }
    },
    reAuthAndOpenCampus() {
      // 清除本地舊 token
      localStorage.removeItem('token');
      // 把目標存在 sessionStorage，授權完成後前端可以根據這個判斷是否需要打開校園系統
      sessionStorage.setItem('pendingCampusRedirect', 'true');

      if (import.meta.env.MODE !== 'production') {
        // 非生產環境走 dev mock 登錄
        window.location.href = baseURL + '/wechat/oauth/callback?code=dev&state=dev';
        return;
      }

      // 生產環境：觸發微信重新授權
      const redirectUri = encodeURIComponent(settings.wechat.redirectUri);
      const corpId = settings.wechat.corpId;
      const agentId = settings.wechat.agentId;
      window.location.href = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${corpId}&redirect_uri=${redirectUri}&response_type=code&scope=snsapi_base&agentid=${agentId}&state=default#wechat_redirect`;
    },

    // 處理學生切換事件（其他頁面發出的）
    handleStudentChanged() {
      this.currentStudentName = localStorage.getItem('currentStudentName') || '';
      console.log('學生已切換，重新獲取未讀通知數量');
      this.fetchUnreadCount();
    },

    // 開啟切換學生彈窗（直接打開，由 StudentSwitchDialog 元件自行拉列表）
    openStudentSwitchDialog() {
      this.studentDialogVisible = true;
    },

    // StudentSwitchDialog 切換成功後的回調
    onStudentSwitched({ studentName }) {
      this.currentStudentName = studentName;
      this.fetchUnreadCount();
    },
  }
}
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  min-height: 100vh;
  width: 100%;
  background: #f8fafc;
  padding: 56px 24px 40px; /* top 留給固定頂部欄的空間 */
  box-sizing: border-box;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

/* ── 頂部學生資訊欄（固定在頁面最上方） ─────────────── */
.student-top-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  border-bottom: 1.5px solid rgba(64, 158, 255, 0.18);
  box-shadow: 0 2px 12px rgba(26, 115, 232, 0.08);
  padding: 0 10px;
  height: 48px;
  box-sizing: border-box;
}

.student-name-area {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.85);
  color: #2563eb;
  border: 1.5px solid rgba(37, 99, 235, 0.35);
  user-select: none;
  overflow: hidden;
}

.student-avatar-icon {
  font-size: 15px;
  flex-shrink: 0;
  color: #1e293b;
}

.student-name-text {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.switch-student-btn {
  margin-right: 0;
  padding: 8px 14px;
  border-radius: 8px;
  background: linear-gradient(135deg, #2563eb 0%, #dbeafe 100%);
  color: #1e3a8a;
  border: none;
  box-shadow: 0 4px 6px rgba(147, 197, 253, 0.2);
  transition: all 0.3s ease;
  white-space: nowrap;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
}

@media (hover: hover) {
  .switch-student-btn:hover {
    background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
    transform: translateY(-2px);
    box-shadow: 0 6px 10px rgba(147, 197, 253, 0.3);
  }
}

.switch-student-btn:active {
  transform: scale(0.96);
  opacity: 0.9;
}


.welcome-section {
  text-align: center;
  margin-top: 15px;
  margin-bottom: 25px;
  width: 100%;
  max-width: 500px;
  animation: fadeInDown 0.8s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.logo-badge {
  width: 130px;
  height: 130px;
  border-radius: 50%; /* Revert back to circular logo as user might prefer it */
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
  padding: 10px;
  box-sizing: border-box;
  overflow: hidden;
}

.school-logo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.welcome-title {
  font-size: 28px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 10px;
  letter-spacing: 0.5px;
}

.welcome-subtitle {
  font-size: 16px;
  color: #64748b;
  margin: 0;
  font-weight: 500;
}

/* 按鈕容器樣式 */
.buttons-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
  max-width: 280px; /* Reduced width */
  animation: fadeInUp 0.8s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.button-wrapper {
  position: relative;
  width: 100%;
}

.button-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.button-icon {
  font-size: 28px;
  line-height: 1;
}

.button-text {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

/* 未讀通知徽章樣式 - 放回右上角 */
.unread-badge {
  position: absolute;
  top: -12px;
  right: -12px;
  background: #ff4d4f;
  color: white;
  border-radius: 20px;
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 800;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.4);
  border: 3px solid #f8fafc; /* Match background to create a cutout effect */
  animation: badgeBounce 2s infinite cubic-bezier(0.2, 0.8, 0.2, 1);
  z-index: 10;
}

/* 徽章脈衝動畫 */
@keyframes badgeBounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-4px);
  }
}

.feature-button {
  height: 85px; /* Restored height */
  border-radius: 20px;
  transition: all 0.2s ease;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  box-sizing: border-box;
  border: none;
  cursor: pointer;
  outline: none;
}

.feature-button:active {
  transform: scale(0.98);
}

.feature-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none !important;
}

.primary-button {
  background: linear-gradient(135deg, #409eff 0%, #1a73e8 100%);
  color: white;
  box-shadow: 0 8px 24px rgba(26, 115, 232, 0.25);
}

.success-button {
  background: linear-gradient(135deg, #67c23a 0%, #4caf50 100%);
  color: white;
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.25);
}

.info-button {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
  box-shadow: 0 8px 24px rgba(245, 158, 11, 0.25);
}

.warning-button {
  background: linear-gradient(135deg, #ff9f43 0%, #ee5a24 100%);
  color: white;
  box-shadow: 0 8px 24px rgba(238, 90, 36, 0.25);
}

/* 動畫效果 */
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

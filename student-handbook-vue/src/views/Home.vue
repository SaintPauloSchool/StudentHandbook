<template>
  <div class="home-container" v-loading.fullscreen.lock="isNavigatingToCampus" element-loading-text="正在驗證並跳轉至校園系統...">

    <!-- 頂部學生資訊欄：固定在頁面最上方，左姓名右切換 -->
    <div class="student-top-bar" v-if="(userType === 0 || userType === 1) && currentStudentName">
      <StudentChip
        chip-class="student-top-bar-chip"
        :avatar-size="50"
        :name="currentStudentName"
        :class-section="currentStudentClassSection"
        :profile-number="currentStudentProfileNumber"
      />
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
        <button class="feature-button attendance-button" @click="goToAttendance">
          <div class="button-content">
            <span class="button-icon">🕐</span>
            <span class="button-text">考勤記錄</span>
          </div>
        </button>
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

    <!-- 頁尾版權資訊 -->
    <div class="footer-info">
      ©2017-2024 Saint Paul School, Macau 澳門聖保祿學校 All rights reserved | {{ version }}
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
import StudentChip from '@/components/StudentChip.vue'
import { isWeChatEnv, saveTokenFromUrl } from '@/utils/wechat.js'

export default {
  name: 'Home',
  components: { StudentSwitchDialog, StudentChip },
  data() {
    const cachedUserType = localStorage.getItem('userType');
    return {
      unreadCount: 0,
      isNavigatingToCampus: false,
      userType: cachedUserType !== null ? parseInt(cachedUserType) : null,
      currentStudentName: localStorage.getItem('currentStudentName') || '',
      currentStudentClassSection: localStorage.getItem('currentStudentClassSection') || '',
      currentStudentProfileNumber: localStorage.getItem('currentStudentProfileNumber') || '',
      studentDialogVisible: false, // 切換彈窗是否顯示
      version: settings.version // 系統版本號
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

    // ② 有了正確的 studentId 後，再拉未讀數，確保數字對應正確的學生
    await this.fetchUnreadCount();

    // 獲取使用者資訊（包含 userType）
    await this.fetchUserInfo();

    // 監聽學生切換事件
    window.addEventListener('studentChanged', this.handleStudentChanged);
  },
  beforeUnmount() {
    // 移除事件監聽器
    window.removeEventListener('studentChanged', this.handleStudentChanged);
  },

  methods: {
    checkTokenFromUrl() {
      const urlParams = new URLSearchParams(window.location.search);
      const state = urlParams.get('state');

      if (saveTokenFromUrl(urlParams)) {
        const urlUserType = localStorage.getItem('userType');
        if (urlUserType !== null) {
          this.userType = parseInt(urlUserType, 10);
        } else {
          this.userType = null;
        }
        ElMessage.success('登錄成功');
      }

      const pendingCampus = sessionStorage.getItem('pendingCampusRedirect') === 'true';
      const campusNotice = state && state.startsWith('campus_notice_');
      if (pendingCampus || campusNotice) {
        const token = localStorage.getItem('token');
        if (!token) return;

        sessionStorage.removeItem('pendingCampusRedirect');

        let targetUrl = settings.campusSystemUrl;
        if (campusNotice) {
          const noticeId = state.replace('campus_notice_', '');
          if (noticeId !== 'root') {
            const baseUrl = settings.campusSystemUrl.endsWith('/') ? settings.campusSystemUrl : settings.campusSystemUrl + '/';
            targetUrl = `${baseUrl}${noticeId}`;
          }
        }

        const campusUrl = `${targetUrl}${targetUrl.includes('?') ? '&' : '?'}token=${encodeURIComponent(token)}`;
        if (campusNotice) {
          window.location.replace(campusUrl);
        } else {
          this.openCampusUrl(campusUrl);
        }
        return;
      }
    },

    // ① 首次登入時，確保默認學生已嵌入 localStorage，並同步顯示姓名
    async ensureDefaultStudent() {
      // 如果已經有緬存的學生 ID，直接同步姓名後返回
      if (localStorage.getItem('currentStudentId')) {
        this.currentStudentName = localStorage.getItem('currentStudentName') || '';
        this.currentStudentClassSection = localStorage.getItem('currentStudentClassSection') || '';
        this.currentStudentProfileNumber = localStorage.getItem('currentStudentProfileNumber') || '';
        if (this.currentStudentName && this.currentStudentClassSection && this.currentStudentProfileNumber) return;
      }

      try {
        const response = await service.get(API_ENDPOINTS.STUDENT_HANDBOOK_STUDENTS);
        if (response.data.code === 200) {
          const relations = response.data.data;
          if (relations && relations.length > 0) {
            const savedId = localStorage.getItem('currentStudentId');
            const matched = savedId ? relations.find(r => r.studentId === savedId) : null;
            const defaultRel = matched || relations[0];
            this.currentStudentName = defaultRel.studentName;
            this.currentStudentClassSection = defaultRel.classSection || '';
            this.currentStudentProfileNumber = defaultRel.studentProfileNumber || '';
            localStorage.setItem('currentStudentId', defaultRel.studentId);
            localStorage.setItem('currentStudentName', defaultRel.studentName);
            localStorage.setItem('currentStudentClassSection', defaultRel.classSection || '');
            localStorage.setItem('currentStudentProfileNumber', defaultRel.studentProfileNumber || '');
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
        const studentId = localStorage.getItem('currentStudentId');

        const params = {};
        if (studentId) {
          params.studentId = studentId;
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
    goToAttendance() {
      this.$router.push('/attendance');
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
          // Token 確實有效，跳轉到校園系統
          const url = `${settings.campusSystemUrl}?token=${encodeURIComponent(token)}`;
          this.openCampusUrl(url);
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
    // 企微/微信 WebView 不支援 window.open，需用同頁跳轉
    openCampusUrl(url) {
      if (isWeChatEnv()) {
        window.location.href = url;
      } else {
        window.open(url, '_blank');
      }
    },
    reAuthAndOpenCampus() {
      localStorage.removeItem('token');
      sessionStorage.setItem('pendingCampusRedirect', 'true');

      if (import.meta.env.MODE !== 'production') {
        window.location.href = baseURL + '/wechat/oauth/callback?code=dev&state=dev';
        return;
      }

      if (!isWeChatEnv()) {
        ElMessage.warning('請先在微信中登錄後再訪問系統');
        this.$router.push('/login');
        return;
      }

      const redirectUri = encodeURIComponent(settings.wechat.redirectUri);
      const corpId = settings.wechat.corpId;
      const agentId = settings.wechat.agentId;
      window.location.href = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${corpId}&redirect_uri=${redirectUri}&response_type=code&scope=snsapi_base&agentid=${agentId}&state=default#wechat_redirect`;
    },

    // 處理學生切換事件（其他頁面發出的）
    handleStudentChanged() {
      this.currentStudentName = localStorage.getItem('currentStudentName') || '';
      this.currentStudentClassSection = localStorage.getItem('currentStudentClassSection') || '';
      this.currentStudentProfileNumber = localStorage.getItem('currentStudentProfileNumber') || '';
      console.log('學生已切換，重新獲取未讀通知數量');
      this.fetchUnreadCount();
    },

    // 開啟切換學生彈窗（直接打開，由 StudentSwitchDialog 元件自行拉列表）
    openStudentSwitchDialog() {
      this.studentDialogVisible = true;
    },

    // StudentSwitchDialog 切換成功後的回調
    onStudentSwitched({ studentName, classSection, studentProfileNumber }) {
      this.currentStudentName = studentName;
      this.currentStudentClassSection = classSection || localStorage.getItem('currentStudentClassSection') || '';
      this.currentStudentProfileNumber = studentProfileNumber || localStorage.getItem('currentStudentProfileNumber') || '';
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
  padding: 96px 24px 40px; /* top 留給固定頂部欄的空間 */
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
  padding: 14px 16px;
  min-height: 88px;
  height: auto;
  box-sizing: border-box;
}

.student-top-bar :deep(.student-top-bar-chip) {
  padding: 12px 18px 12px 10px;
  gap: 14px;
}

.student-top-bar :deep(.student-top-bar-chip .student-chip__avatar) {
  width: 50px;
  height: 50px;
}

.student-top-bar :deep(.student-top-bar-chip .student-chip__text) {
  gap: 12px;
}

.switch-student-btn {
  margin-right: 0;
  padding: 10px 16px;
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

.attendance-button {
  background: linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%);
  color: white;
  box-shadow: 0 8px 24px rgba(109, 40, 217, 0.25);
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

.footer-info {
  margin-top: auto;
  padding-top: 40px;
  font-size: 11px;
  color: #94a3b8;
  text-align: center;
  line-height: 1.6;
  letter-spacing: 0.2px;
  width: 100%;
}
</style>
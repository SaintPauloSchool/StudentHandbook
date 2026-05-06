<template>
  <div class="home-container" v-loading.fullscreen.lock="isNavigatingToCampus" element-loading-text="正在驗證並跳轉至校園系統...">
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

      <div class="button-wrapper" v-if="userType === 2 || userType === null">
        <button class="feature-button warning-button" @click="goToCampusSystem">
          <div class="button-content">
            <span class="button-icon">🏫</span>
            <span class="button-text">校園系統</span>
          </div>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import service from '@/utils/request.js'
import {ElMessage} from 'element-plus'
import settings from '@/config/settings' // 导入全局配置设置
import { API_ENDPOINTS, baseURL } from '@/config/api.js' // 导入API端点配置

export default {
  name: 'Home',
  data() {
    const cachedUserType = localStorage.getItem('userType');
    return {
      unreadCount: 0, // 未读通知数量
      isNavigatingToCampus: false, // 是否正在跳轉至校園系統
      userType: cachedUserType !== null ? parseInt(cachedUserType) : null // 0: 學生, 1: 家長, 2: 員工
    }
  },
  mounted() {
    // 检查URL参数中是否有token（来自微信授权回调）
    this.checkTokenFromUrl();
    
    // 根据配置决定是否执行Token验证
    if (settings.enableTokenAuth) {
      // 检查是否存在token，如果没有则重定向到登录页面
      this.checkToken();
    }

    // 获取未读通知数量
    this.fetchUnreadCount();

    // 獲取使用者資訊（包含 userType）
    this.fetchUserInfo();
  },

  methods: {
    // 检查URL参数中的token和userType
    checkTokenFromUrl() {
      const urlParams = new URLSearchParams(window.location.search);
      const token = urlParams.get('token');
      const urlUserType = urlParams.get('userType');

      if (token) {
        // 保存token到本地存储，同时记录过期时间 (7天)
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

        // 清除URL中的参数，避免在地址栏显示敏感信息
        urlParams.delete('token');
        urlParams.delete('userType');
        const newUrl = window.location.pathname +
            (urlParams.toString() ? '?' + urlParams.toString() : '') +
            window.location.hash;
        window.history.replaceState({}, document.title, newUrl);

        // 若之前是因為 token 過期後重新授權，授權完成後自動打開校園系統
        if (sessionStorage.getItem('pendingCampusRedirect') === 'true') {
          sessionStorage.removeItem('pendingCampusRedirect');
          const campusUrl = `${settings.campusSystemUrl}?token=${encodeURIComponent(token)}`;
          console.log('重新授權完成，自動跳轉到校園系統:', campusUrl);
          window.open(campusUrl, '_blank');
        } else {
          ElMessage.success('登錄成功');
        }
      }
    },

    // 检查是否存在token
    checkToken() {
      const token = localStorage.getItem('token');
      if (!token) {
        // 如果没有token，重定向到登录页面
        this.$router.push('/login');
      }
    },
    
    // 获取未读通知数量
    async fetchUnreadCount() {
      try {
        const response = await service.get(API_ENDPOINTS.NOTICE_UNREAD_COUNT);
        const res = response.data;
        if (res.code === 200 && res.data) {
          this.unreadCount = res.data.unreadCount || 0;
          console.log('设置未读数量为:', this.unreadCount); // 调试信息
        }
      } catch (error) {
        console.error('获取未读通知数量失败:', error);
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
      // 跳转到家校通知页面
      this.$router.push('/notice');
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

      if (import.meta.env.MODE === 'development') {
        // 開發環境：直接走 dev callback，不走真實微信授權
        window.location.href = baseURL + '/wechat/oauth/callback?code=dev&state=dev';
        return;
      }

      // 生產環境：觸發微信重新授權
      const redirectUri = encodeURIComponent(settings.wechat.redirectUri);
      const corpId = settings.wechat.corpId;
      const agentId = settings.wechat.agentId;
      window.location.href = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${corpId}&redirect_uri=${redirectUri}&response_type=code&scope=snsapi_base&agentid=${agentId}&state=default#wechat_redirect`;
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
  background: #f8fafc; /* Very light, clean background for full screen */
  padding: 40px 24px;
  box-sizing: border-box;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
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

/* 未读通知徽章样式 - 放回右上角 */
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

/* 徽章脉冲动画 */
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

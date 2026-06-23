<template>
  <div class="login-container">
    <div class="login-form">
      <div class="logo-section">
        <img src="../logo/sp.jpg" alt="School Logo" class="school-logo-img">
        <h4 v-if="!showError" class="welcome-title">請等候片刻，正在自動驗證。。。</h4>
      </div>

      <div v-if="loginLoading" class="loading-overlay">
        <div class="loading-spinner"></div>
        <p>正在登錄中...</p>
      </div>

      <div v-if="showError" class="error-overlay">
        <div class="error-content">
          <h3>⚠️ 授權失敗</h3>
          <p>{{ errorMessage }}</p>
          <button @click="retryLogin" class="retry-button">重新登錄</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import service from '@/utils/request.js'
import {ElMessage} from 'element-plus'
import settings from '@/config/settings' // 導入全局配置設置
import { baseURL, API_ENDPOINTS } from '@/config/api.js' // 導入 API 基礎路徑
import {normalizeRedirectUrl} from '@/utils/path.js'
import {isWeChatEnv, saveTokenFromUrl} from '@/utils/wechat.js'

export default {
  name: 'Login',
  data() {
    return {
      loginLoading: false,
      showError: false,
      errorMessage: '無法進入系統，請聯繫學校管理員！！'
    }
  },
  async mounted() {
    // 檢查URL參數，看是否是錯誤狀態
    this.checkUrlError();

    // 如果是錯誤狀態，不執行自動登錄
    if (this.showError) {
      return;
    }

    // 檢查URL參數中的token（來自微信授權回調）
    // 如果 URL 中已帶有 token，保存後直接跳首頁，不再繼續執行登錄流程
    if (this.checkTokenFromUrl()) {
      return;
    }

    // 檢查URL參數中的授權code
    if (await this.checkWeChatAuthCode()) {
      return;
    }

    if (settings.enableWeChatAuth) {
      if (await this.tryExistingToken()) {
        return;
      }

      if (isWeChatEnv()) {
        await this.autoWechatLogin();
      }
    } else {
      this.$router.push('/');
    }
  },
  methods: {
    async tryExistingToken() {
      const token = localStorage.getItem('token');
      if (!token) {
        return false;
      }

      const expire = localStorage.getItem('token_expire');
      if (expire && Date.now() > parseInt(expire, 10)) {
        localStorage.removeItem('token');
        localStorage.removeItem('token_expire');
        return false;
      }

      try {
        const response = await service.get(API_ENDPOINTS.VALIDATE_TOKEN);
        if (response.data.code === 200) {
          const redirectUrl = sessionStorage.getItem('redirect_url') || '/';
          sessionStorage.removeItem('redirect_url');
          this.$router.push(normalizeRedirectUrl(redirectUrl));
          return true;
        }
      } catch (error) {
        console.warn('本地 token 已失效:', error);
      }

      return false;
    },

    // 檢查URL參數中的token，找到則保存並返回 true（通知 mounted 提前結束）
    checkTokenFromUrl() {
      const urlParams = new URLSearchParams(window.location.search);

      if (saveTokenFromUrl(urlParams)) {
        ElMessage.success('登錄成功');
        const redirectUrl = sessionStorage.getItem('redirect_url') || '/';
        sessionStorage.removeItem('redirect_url');
        this.$router.push(normalizeRedirectUrl(redirectUrl));
        return true;
      }
      return false;
    },

    // 檢查URL參數中是否有微信授權code
    async checkWeChatAuthCode() {
      const urlParams = new URLSearchParams(window.location.search);
      const code = urlParams.get('code');
      const state = urlParams.get('state');

      // 檢查是否有錯誤參數
      const errcode = urlParams.get('errcode');
      if (errcode) {
        console.error(`微信授權錯誤，錯誤碼: ${errcode}`);
        ElMessage.error('微信授權失敗');
        return;
      }

      if (code) {
        console.log('檢測到微信授權code，開始登錄流程');
        this.loginLoading = true;

        try {
          const response = await service.get(`${baseURL}/wechat/oauth/callback?code=${code}&state=${state || 'default'}`);

          if (response.data.code === 200) {
            ElMessage.success('登錄成功');
            const redirectUrl = sessionStorage.getItem('redirect_url') || '/';
            sessionStorage.removeItem('redirect_url');
            this.$router.push(normalizeRedirectUrl(redirectUrl));
          } else {
            ElMessage.error(response.data.msg || '登錄失敗');
          }
        } catch (error) {
          console.error('登錄請求失敗:', error);
          ElMessage.error('登錄請求失敗');
        } finally {
          this.loginLoading = false;
        }
        return true;
      }
      return false;
    },

    // 檢查URL參數，確定是否顯示錯誤
    checkUrlError() {
      const urlParams = new URLSearchParams(window.location.search);
      const error = urlParams.get('error');

      if (error) {
        this.showError = true;
        this.errorMessage = '授權失敗無法進入系統，請聯繫學校管理員';
      }

      return !!error;
    },

    // 重試登錄
    retryLogin() {
      this.showError = false;
      this.errorMessage = '授權失敗無法進入系統，請聯繫學校管理員';
      if (settings.enableWeChatAuth) {
        if (isWeChatEnv()) {
          this.autoWechatLogin();
        }
      } else {
        this.$router.push('/');
      }
    },

    // 自動微信登錄
    async autoWechatLogin() {
      const urlParams = new URLSearchParams(window.location.search);
      let state = 'default';
      const redirectToCampus = urlParams.get('redirect_to_campus');
      if (redirectToCampus) {
          state = 'campus_notice_' + redirectToCampus;
      }
      
      // 如果是開發環境，直接調用模擬的 callback，透過 code=dev 讓後端返回配置的 token
      if (import.meta.env.MODE !== 'production') {
        // 非生產環境（本地 vite dev 或 build:dev 部署到測試服）都走 mock 登錄，跳過微信驗證
        console.log('非生產環境，直接跳轉到模擬登錄');
        this.loginLoading = true;
        window.location.href = baseURL + '/wechat/oauth/callback?code=dev&state=' + encodeURIComponent(state);
        return;
      }

      // 僅在微信內自動發起 OAuth
      if (isWeChatEnv()) {
        console.log('在微信環境中，自動觸發微信授權');
        await this.getWeChatUserInfoByOAuth(state);
      }
    },

    // 通過OAuth2方式獲取微信用戶信息
    async getWeChatUserInfoByOAuth(state) {
      try {
        console.log('構建微信授權鏈接');
        // 從配置文件中讀取企業微信相關參數
        const redirectUri = encodeURIComponent(settings.wechat.redirectUri);
        const corpId = settings.wechat.corpId;
        const agentId = settings.wechat.agentId;
        const safeState = state || 'default';

        // 構造適合手機端的企業微信OAuth2授權鏈接，使用動態狀態
        console.log('跳轉到微信授權頁面: https://open.weixin.qq.com/connect/oauth2/authorize');
        // 重定向到授權頁面
        window.location.href = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${corpId}&redirect_uri=${redirectUri}&response_type=code&scope=snsapi_base&agentid=${agentId}&state=${safeState}#wechat_redirect`;
      } catch (error) {
        console.error('發起微信授權失敗: ' + error.message);
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #74b9ff, #0984e3);
  padding: 20px;
  box-sizing: border-box;
}

.login-form {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.login-form::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.1) 0, transparent 70%);
  z-index: 0;
}

.logo-section {
  text-align: center;
  margin-bottom: 30px;
  position: relative;
  z-index: 1;
}

.school-logo-img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 15px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.welcome-title {
  font-size: 50px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(255, 245, 245, 0.95);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 20;
  border-radius: 10px;
}

.error-content {
  text-align: center;
  padding: 30px;
  max-width: 80%;
}

.error-content h3 {
  color: #e74c3c;
  margin-bottom: 15px;
  font-size: 24px;
}

.error-content p {
  color: #666;
  margin: 5px 0;
  font-size: 16px;
}

.retry-button {
  margin-top: 20px;
  padding: 10px 20px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.retry-button:hover {
  background-color: #2980b9;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 10;
  border-radius: 10px;
}

.loading-spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409eff;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.loading-overlay p {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .login-container {
    padding: 10px;
  }

  .login-form {
    padding: 20px;
    max-width: 100%;
  }

  .school-logo-img {
    width: 80px;
    height: 80px;
  }

  .welcome-title {
    font-size: 30px;
  }

}
</style>
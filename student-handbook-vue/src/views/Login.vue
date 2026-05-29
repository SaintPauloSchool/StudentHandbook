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
import settings from '@/config/settings' // 导入全局配置设置
import { baseURL } from '@/config/api.js' // 导入 API 基础路径

export default {
  name: 'Login',
  data() {
    return {
      loginLoading: false,
      showError: false,
      errorMessage: '無法進入系統，請聯繫學校管理員！！'
    }
  },
  mounted() {
    // 检查URL参数，看是否是错误状态
    this.checkUrlError();

    // 如果是错误状态，不执行自动登录
    if (this.showError) {
      return;
    }

    // 检查URL参数中的token（来自微信授权回调）
    // 如果 URL 中已帶有 token，保存後直接跳首頁，不再繼續執行登錄流程
    if (this.checkTokenFromUrl()) {
      return;
    }

    // 检查URL参数中的授权code
    this.checkWeChatAuthCode();

    // 根据配置决定是否执行微信登录流程
    if (settings.enableWeChatAuth) {
      // 自动触发微信登录流程
      this.autoWechatLogin();
    } else {
      // 如果未启用微信验证，则直接跳转到首页
      this.$router.push('/');
    }
  },
  methods: {
    // 检查URL参数中的token，找到則保存並返回 true（通知 mounted 提前結束）
    checkTokenFromUrl() {
      const urlParams = new URLSearchParams(window.location.search);
      const token = urlParams.get('token');

      if (token) {
        // 保存token到本地存储，同时记录过期时间 (7天)
        localStorage.setItem('token', token);
        localStorage.setItem('token_expire', (Date.now() + 7 * 24 * 60 * 60 * 1000).toString());

        // 清除URL中的token参数，避免在地址栏显示敏感信息
        urlParams.delete('token');
        const newUrl = window.location.pathname +
            (urlParams.toString() ? '?' + urlParams.toString() : '') +
            window.location.hash;
        window.history.replaceState({}, document.title, newUrl);

        ElMessage.success('登錄成功');
        // 获取之前保存的重定向地址
        const redirectUrl = sessionStorage.getItem('redirect_url') || '/';
        sessionStorage.removeItem('redirect_url');
        // 跳转
        this.$router.push(redirectUrl);
        return true; // ← 告知調用方已處理，無需繼續登錄流程
      }
      return false;
    },

    // 检查URL参数中是否有微信授权code
    async checkWeChatAuthCode() {
      const urlParams = new URLSearchParams(window.location.search);
      const code = urlParams.get('code');
      const state = urlParams.get('state');

      // 检查是否有错误参数
      const errcode = urlParams.get('errcode');
      if (errcode) {
        console.error(`微信授权错误，错误码: ${errcode}`);
        ElMessage.error('微信授权失败');
        return;
      }

      if (code) {
        console.log('检测到微信授权code，开始登录流程');
        this.loginLoading = true;

        try {
          const response = await service.get(`${baseURL}/wechat/oauth/callback?code=${code}&state=${state || 'default'}`);

          if (response.data.code === 200) {
            // 由于request.js中的响应拦截器已经处理了token的保存
            // 这里不再需要手动保存token
            ElMessage.success('登錄成功');
            // 获取之前保存的重定向地址
            const redirectUrl = sessionStorage.getItem('redirect_url') || '/';
            sessionStorage.removeItem('redirect_url');
            // 跳转
            this.$router.push(redirectUrl);
          } else {
            ElMessage.error(response.data.msg || '登錄失敗');
          }
        } catch (error) {
          console.error('登录请求失败:', error);
          ElMessage.error('登錄請求失敗');
        } finally {
          this.loginLoading = false;
        }
      }
    },

    // 检查URL参数，确定是否显示错误
    checkUrlError() {
      const urlParams = new URLSearchParams(window.location.search);
      const error = urlParams.get('error');

      if (error) {
        this.showError = true;
        this.errorMessage = '授權失敗無法進入系統，請聯繫學校管理員';
      }

      return !!error;
    },

    // 重试登录
    retryLogin() {
      this.showError = false;
      this.errorMessage = '授權失敗無法進入系統，請聯繫學校管理員';
      if (settings.enableWeChatAuth) {
        // 重新触发微信登录
        this.autoWechatLogin();
      } else {
        // 如果未启用微信验证，则直接跳转到首页
        this.$router.push('/');
      }
    },

    // 自动微信登录
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

      // 检查是否在微信环境中
      const isWeChat = navigator.userAgent.includes('MicroMessenger');

      if (isWeChat) {
        console.log('在微信环境中，自动触发微信授权');

        // 尝试通过OAuth2方式获取用户信息
        await this.getWeChatUserInfoByOAuth(state);
      } else {
        console.log('非微信环境，显示提示信息');
        ElMessage.warning('请在微信或企业微信环境中打开应用');
      }
    },

    // 通过OAuth2方式获取微信用户信息
    async getWeChatUserInfoByOAuth(state) {
      try {
        console.log('构建微信授权链接');
        // 从配置文件中读取企业微信相关参数
        const redirectUri = encodeURIComponent(settings.wechat.redirectUri);
        const corpId = settings.wechat.corpId;
        const agentId = settings.wechat.agentId;
        const safeState = state || 'default';

        // 构造适合手机端的企业微信OAuth2授权链接，使用动态状态
        console.log('跳转到微信授权页面: https://open.weixin.qq.com/connect/oauth2/authorize');
        // 重定向到授权页面
        window.location.href = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${corpId}&redirect_uri=${redirectUri}&response_type=code&scope=snsapi_base&agentid=${agentId}&state=${safeState}#wechat_redirect`;
      } catch (error) {
        console.error('发起微信授权失败: ' + error.message);
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

/* 响应式设计 */
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
import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import settings from '@/config/settings' // 導入全局配置設置
import { checkAndClearCache } from '@/utils/versionCheck'

// Element Plus imports
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhTw from 'element-plus/es/locale/lang/zh-tw'

// ── 版本檢查（必須在 Vue 掛載前完成）──────────────────────────
// 從服務器拉取 /version.json（no-store 強制繞過 WebView 緩存）
// 若版本號與 localStorage 不同，立即 replace 到帶 ?_v= 的新 URL
// WeChat WebView 遇到新 URL 必須重新從服務器拉 index.html，從而載入新代碼
const isReloading = await checkAndClearCache().catch(() => false);
if (isReloading) {
    // 正在重載，停止後續 Vue 初始化，避免閃爍
    throw new Error('[版本更新] 頁面即將重載，停止 Vue 初始化');
}
// ─────────────────────────────────────────────────────────────

// 創建Vue應用實例
const app = createApp(App)

// 使用Element Plus
app.use(ElementPlus, {
    locale: zhTw,
})

// 配置路由守衛，確保訪問受保護頁面時已登錄
router.beforeEach((to, from, next) => {
    // 檢查是否啓用Token驗證
    if (settings.enableTokenAuth) {
        // 定義不需要驗證token的頁面
        const publicPages = ['/', '/login', '/register']
        const isPublicPage = publicPages.includes(to.path)
        const token = localStorage.getItem('token')

        // 檢查URL參數中的token（來自微信授權回調）
        const urlParams = new URLSearchParams(window.location.search);
        const tokenFromUrl = urlParams.get('token');

        // 如果URL中有token，先保存到localStorage
        if (tokenFromUrl) {
            localStorage.setItem('token', tokenFromUrl);
            // 更新URL，移除token參數
            urlParams.delete('token');
            const newUrl = window.location.pathname +
                (urlParams.toString() ? '?' + urlParams.toString() : '') +
                window.location.hash;
            window.history.replaceState({}, document.title, newUrl);

            // 如果之前有保存的跳轉路徑，則跳轉過去
            const redirectUrl = sessionStorage.getItem('redirect_url');
            if (redirectUrl) {
                sessionStorage.removeItem('redirect_url');
                next(redirectUrl);
                return;
            } else if (to.path === '/login') {
                // 如果沒有保存的路徑，但當前是登錄頁，則預設跳轉到首頁
                next('/');
                return;
            }

            // 有token，允許訪問
            next();
            return;
        }

        // 如果是公共頁面且已登錄，直接訪問
        if (isPublicPage) {
            next()
        } else {
            // 如果不是公共頁面，需要驗證token
            if (token) {
                // 有token，允許訪問
                next()
            } else {
                // 沒有token，重定向到登錄頁面前，先保存當前路徑以便登錄後跳轉回來
                sessionStorage.setItem('redirect_url', to.fullPath)
                next('/login')
            }
        }
    } else {
        // 如果未啓用Token驗證，則允許所有路由訪問
        next();
    }
})

// 配置路由
app.use(router)

// 掛載應用
app.mount('#app')

// 動態加載企業微信JS-SDK
function loadWeChatSDK() {
    // 檢查是否啓用了微信驗證
    if (settings.enableWeChatAuth) {
        // 檢查是否在微信相關環境中（包括微信和企業微信）
        const isWeChat = navigator.userAgent.includes('MicroMessenger');

        // 無論是在普通微信還是企業微信環境中，都嘗試加載企業微信JS-SDK
        // 因爲企業微信應用在微信中打開時也需要使用企業微信JS-SDK
        if (isWeChat) {
            const script = document.createElement('script');
            script.src = 'https://res.wx.qq.com/open/js/jweixin-1.2.0.js';
            script.onload = () => {
                console.log('企業微信JS-SDK加載成功');
            };
            script.onerror = () => {
                console.error('企業微信JS-SDK加載失敗');
            };
            document.head.appendChild(script);
        }
    }
}

// 頁面加載時嘗試加載企業微信SDK
loadWeChatSDK();
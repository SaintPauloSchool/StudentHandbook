import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import settings from '@/config/settings'
import {saveTokenFromUrl, clearAuthSession, sanitizeRedirectUrl} from '@/utils/wechat.js'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhTw from 'element-plus/es/locale/lang/zh-tw'

const app = createApp(App)

app.use(ElementPlus, {
    locale: zhTw,
})

router.beforeEach((to, from, next) => {
    if (settings.enableTokenAuth) {
        // 僅登錄頁公開；首頁 / 需 token（OAuth 回跳帶 ?token= 時會先寫入再放行）
        const publicPages = ['/login']
        const isPublicPage = publicPages.includes(to.path)
        const token = localStorage.getItem('token')

        // 優先用路由目標 query（兼容 SPA 跳轉），並回退 window.location
        const urlParams = new URLSearchParams(window.location.search)
        if (to.query.token) {
            urlParams.set('token', String(to.query.token))
        }
        if (to.query.userType !== undefined && to.query.userType !== null && to.query.userType !== '') {
            urlParams.set('userType', String(to.query.userType))
        }

        if (saveTokenFromUrl(urlParams)) {
            const redirectUrl = sessionStorage.getItem('redirect_url');
            if (redirectUrl) {
                sessionStorage.removeItem('redirect_url');
                next(sanitizeRedirectUrl(redirectUrl));
                return;
            } else if (to.path === '/login') {
                next('/');
                return;
            }

            // 去掉地址欄 token 後，同步清掉路由裡的 query，避免後續又寫回 URL
            if (to.query.token || to.query.userType !== undefined) {
                const cleanedQuery = { ...to.query }
                delete cleanedQuery.token
                delete cleanedQuery.userType
                next({ path: to.path, query: cleanedQuery, hash: to.hash, replace: true })
                return
            }

            next();
            return;
        }

        if (isPublicPage) {
            next()
        } else {
            if (token) {
                next()
            } else {
                // 無 token 時清掉殘留學生緩存，避免下一屏誤顯示別人孩子
                clearAuthSession()
                sessionStorage.setItem('redirect_url', sanitizeRedirectUrl(to.fullPath))
                next('/login')
            }
        }
    } else {
        next();
    }
})

app.use(router)
app.mount('#app')

if (settings.enableWeChatAuth && navigator.userAgent.includes('MicroMessenger')) {
    const script = document.createElement('script');
    script.src = 'https://res.wx.qq.com/open/js/jweixin-1.2.0.js';
    document.head.appendChild(script);
}

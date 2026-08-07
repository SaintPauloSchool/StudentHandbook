import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import settings from '@/config/settings'
import {saveTokenFromUrl} from '@/utils/wechat.js'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhTw from 'element-plus/es/locale/lang/zh-tw'

const app = createApp(App)

app.use(ElementPlus, {
    locale: zhTw,
})

router.beforeEach((to, from, next) => {
    if (settings.enableTokenAuth) {
        const publicPages = ['/', '/login']
        const isPublicPage = publicPages.includes(to.path)
        const token = localStorage.getItem('token')

        const urlParams = new URLSearchParams(window.location.search);

        if (saveTokenFromUrl(urlParams)) {
            const redirectUrl = sessionStorage.getItem('redirect_url');
            if (redirectUrl) {
                sessionStorage.removeItem('redirect_url');
                next(redirectUrl);
                return;
            } else if (to.path === '/login') {
                next('/');
                return;
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
                sessionStorage.setItem('redirect_url', to.fullPath)
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

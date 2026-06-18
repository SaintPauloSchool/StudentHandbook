import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import settings from '@/config/settings'
import {getDoubledVersionFix, normalizeRedirectUrl} from '@/utils/path.js'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhTw from 'element-plus/es/locale/lang/zh-tw'

const app = createApp(App)

app.use(ElementPlus, {
    locale: zhTw,
})

router.beforeEach((to, from, next) => {
    const fixedPath = getDoubledVersionFix()
    if (fixedPath !== null) {
        const query = Object.fromEntries(new URLSearchParams(window.location.search))
        return next({
            path: fixedPath,
            query,
            hash: window.location.hash,
            replace: true
        })
    }

    if (settings.enableTokenAuth) {
        const publicPages = ['/', '/login', '/register']
        const isPublicPage = publicPages.includes(to.path)
        const token = localStorage.getItem('token')

        const urlParams = new URLSearchParams(window.location.search);
        const tokenFromUrl = urlParams.get('token');

        if (tokenFromUrl) {
            localStorage.setItem('token', tokenFromUrl);
            urlParams.delete('token');
            const newUrl = window.location.pathname +
                (urlParams.toString() ? '?' + urlParams.toString() : '') +
                window.location.hash;
            window.history.replaceState({}, document.title, newUrl);

            const redirectUrl = sessionStorage.getItem('redirect_url');
            if (redirectUrl) {
                sessionStorage.removeItem('redirect_url');
                next(normalizeRedirectUrl(redirectUrl));
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

import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import settings from '@/config/settings' // 导入全局配置设置

// Element Plus imports
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 创建Vue应用实例
const app = createApp(App)

// 使用Element Plus
app.use(ElementPlus)

// 配置路由守卫，确保访问受保护页面时已登录
router.beforeEach((to, from, next) => {
    // 检查是否启用Token验证
    if (settings.enableTokenAuth) {
        // 定义不需要验证token的页面
        const publicPages = ['/', '/login', '/register']
        const isPublicPage = publicPages.includes(to.path)
        const token = localStorage.getItem('token')

        // 检查URL参数中的token（来自微信授权回调）
        const urlParams = new URLSearchParams(window.location.search);
        const tokenFromUrl = urlParams.get('token');

        // 如果URL中有token，先保存到localStorage
        if (tokenFromUrl) {
            localStorage.setItem('token', tokenFromUrl);
            // 更新URL，移除token参数
            urlParams.delete('token');
            const newUrl = window.location.pathname +
                (urlParams.toString() ? '?' + urlParams.toString() : '') +
                window.location.hash;
            window.history.replaceState({}, document.title, newUrl);
            // 有token，允许访问
            next();
            return;
        }

        // 如果是公共页面且已登录，直接访问
        if (isPublicPage) {
            next()
        } else {
            // 如果不是公共页面，需要验证token
            if (token) {
                // 有token，允许访问
                next()
            } else {
                // 没有token，重定向到登录页面
                next('/login')
            }
        }
    } else {
        // 如果未启用Token验证，则允许所有路由访问
        next();
    }
})

// 配置路由
app.use(router)

// 挂载应用
app.mount('#app')

// 动态加载企业微信JS-SDK
function loadWeChatSDK() {
    // 检查是否启用了微信验证
    if (settings.enableWeChatAuth) {
        // 检查是否在微信相关环境中（包括微信和企业微信）
        const isWeChat = navigator.userAgent.includes('MicroMessenger');

        // 无论是在普通微信还是企业微信环境中，都尝试加载企业微信JS-SDK
        // 因为企业微信应用在微信中打开时也需要使用企业微信JS-SDK
        if (isWeChat) {
            const script = document.createElement('script');
            script.src = 'https://res.wx.qq.com/open/js/jweixin-1.2.0.js';
            script.onload = () => {
                console.log('企业微信JS-SDK加载成功');
            };
            script.onerror = () => {
                console.error('企业微信JS-SDK加载失败');
            };
            document.head.appendChild(script);
        }
    }
}

// 页面加载时尝试加载企业微信SDK
loadWeChatSDK();
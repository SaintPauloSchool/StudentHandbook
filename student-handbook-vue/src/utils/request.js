// 封装axios的请求工具函数
import axios from 'axios'
import {ElMessage} from 'element-plus' // 导入Element Plus的消息组件
import settings from '@/config/settings' // 导入全局配置设置
import MD5 from 'crypto-js/md5' // 导入 MD5 用于计算签名

// 生成唯一标识符(UUID的简易实现)
const generateNonce = () => {
    if (typeof crypto !== 'undefined' && crypto.randomUUID) {
        return crypto.randomUUID().replace(/-/g, '');
    }
    return 'xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
};

// 创建axios实例
const getBaseURL = () => {
    // API_ENDPOINTS已经包含了完整的路径，所以这里只需要返回空字串
    return '';
};

const service = axios.create({
    baseURL: getBaseURL(), // api的base_url
    timeout: 30000 // 请求超时时间
});

// request拦截器
service.interceptors.request.use(
    config => {
        // 在发送请求之前做些什么
        if (settings.enableTokenAuth) { // 只有在启用Token验证时才添加token
            const token = localStorage.getItem('token')
            if (token) {
                // 让每个请求携带自定义token
                config.headers['Authorization'] = 'Bearer ' + token
            }
        }
        
        // API 安全校驗拦截器
        const timestamp = Date.now().toString();
        const nonce = generateNonce();
        const appSecret = settings.appSecret;
        const signature = MD5(appSecret + timestamp + nonce).toString();

        config.headers['x-timestamp'] = timestamp;
        config.headers['x-nonces'] = nonce;
        config.headers['x-signature'] = signature;

        return config
    },
    error => {
        // Do something with request error
        console.log(error) // for debug
        Promise.reject(error)
    }
)

// response拦截器
service.interceptors.response.use(
    response => {
        // 检查响应数据结构
        const res = response.data;

        // 如果后端返回了token信息，保存到本地存储
        if (res.code === 200 && res.data && res.data.token) {
            // 将token保存到本地存储
            localStorage.setItem('token', res.data.token);
        }

        return response;
    },
    error => {
        console.log('err' + error)// for debug
        if (settings.enableTokenAuth) { // 只有在启用Token验证时才进行跳转
            if (error.response && error.response.status === 401) {
                // token过期/无效，跳转到登录页面
                localStorage.removeItem('token')
                // 重定向到登录页面
                window.location.href = '/login'
                ElMessage.error('請先登錄')
            } else if (error.response && error.response.status === 403) {
                // 无权限访问
                ElMessage.error('無權限訪問資源')
            }
        }
        return Promise.reject(error)
    }
)

export default service
// 封裝axios的請求工具函數
import axios from 'axios'
import {ElMessage} from 'element-plus' // 導入Element Plus的消息組件
import settings from '@/config/settings' // 導入全局配置設置
import MD5 from 'crypto-js/md5' // 導入 MD5 用於計算籤名
import {stripVersionPrefix, versionedPath} from '@/utils/path.js'

// 生成唯一標識符(UUID的簡易實現)
const generateNonce = () => {
    if (typeof crypto !== 'undefined' && crypto.randomUUID) {
        return crypto.randomUUID().replace(/-/g, '');
    }
    return 'xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
};

// 創建axios實例
const getBaseURL = () => {
    // API_ENDPOINTS已經包含了完整的路徑，所以這裡只需要返回空字串
    return '';
};

const service = axios.create({
    baseURL: getBaseURL(), // api的base_url
    timeout: 30000 // 請求超時時間
});

// request攔截器
service.interceptors.request.use(
    config => {
        // 在發送請求之前做些什麼
        if (settings.enableTokenAuth) { // 只有在啓用Token驗證時才添加token
            const token = localStorage.getItem('token')
            if (token) {
                // 讓每個請求攜帶自定義token
                config.headers['Authorization'] = 'Bearer ' + token
            }
        }
        
        // API 安全校驗攔截器
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

// response攔截器
service.interceptors.response.use(
    response => {
        // 檢查響應數據結構
        const res = response.data;

        // 如果後端返回了token信息，保存到本地存儲
        if (res.code === 200 && res.data && res.data.token) {
            // 將token保存到本地存儲
            localStorage.setItem('token', res.data.token);
        }

        return response;
    },
    error => {
        console.log('err' + error)// for debug
        if (settings.enableTokenAuth) { // 只有在啓用Token驗證時才進行跳轉
            if (error.response && error.response.status === 401) {
                // token過期/無效，跳轉到登錄頁面
                localStorage.removeItem('token')
                // 記住當前的 URL 以便登錄後跳轉回去
                sessionStorage.setItem(
                    'redirect_url',
                    stripVersionPrefix(window.location.pathname) +
                    window.location.search +
                    window.location.hash
                )
                window.location.href = versionedPath('/login')
                ElMessage.error('請先登錄')
            } else if (error.response && error.response.status === 403) {
                // 無權限訪問
                ElMessage.error('無權限訪問資源')
            }
        }
        return Promise.reject(error)
    }
)

export default service
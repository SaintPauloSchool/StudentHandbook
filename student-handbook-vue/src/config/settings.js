// 前端全局配置設置
export default {
    // 系統版本號
    version: 'V 1.0.0',

    // 微信驗證開關
    enableWeChatAuth: true,

    // Token驗證開關
    enableTokenAuth: true,

    // 登錄頁面路徑
    loginPath: '/login',

    // 校園系統跳轉URL (根據環境區分)
    // 只有 production 打包才指向生產地址，其餘（dev/test）均指向測試服
    campusSystemUrl: import.meta.env.MODE === 'production'
        ? 'http://tals-wcapp.esp.edu.mo/school-management-system/'
        //: 'http://localhost:3001/school-management-system/',
        : 'http://10.32.96.55:8082/school-management-system/',

    // API 安全校驗密鑰
    appSecret: 'HVc1D4MU69UDMr1g',

    // 學生ID加密鹽值（用於微信消息通知跳轉）
    studentIdEncryptionSalt: '5nQtaatpcOs9iKsM',

    // 企業微信配置
    wechat: {
        corpId: 'ww04fad852e91fd490', // 企業微信應用ID
        agentId: '1000033', // 企業微信應用agentId
        // 授權回調地址 (由於只在生產環境走真實微信授權，這裡配置生產環境的回調地址)
        redirectUri: 'https://mo-stu-sys.org-assistant.com/sp-api/wechat/oauth/callback'
    }
}
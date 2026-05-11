// 前端全局配置设置
export default {
    // 微信验证开关
    enableWeChatAuth: true,

    // Token验证开关
    enableTokenAuth: true,

    // 登录页面路径
    loginPath: '/login',

    // 校園系統跳轉URL (根據環境區分)
    campusSystemUrl: import.meta.env.MODE === 'production' 
        ? 'http://10.32.96.55:8082/school-management-system/'
        : 'http://localhost:3001/school-management-system/',

    // API 安全校驗密鑰
    appSecret: 'HVc1D4MU69UDMr1g',

    // 学生ID加密盐值（用于微信消息通知跳转）
    studentIdEncryptionSalt: '5nQtaatpcOs9iKsM',

    // 企业微信配置
    wechat: {
        corpId: 'ww04fad852e91fd490', // 企业微信应用ID
        agentId: '1000033', // 企业微信应用agentId
        // 授权回调地址 (由于只在生产环境走真实微信授权，这里配置生产环境的回调地址)
        redirectUri: 'https://mo-stu-sys.org-assistant.com/sp-api/wechat/oauth/callback'
    }
}
// API 配置文件

// 定义通用的API端点路径
const apiEndpoints = {
    STUDENT_HANDBOOK_PAST_MONTH: '/system/handbook/pastMonth',
    STUDENT_HANDBOOK_TODAY: '/system/handbook/today',
    STUDENT_HANDBOOK_NEXT_SEVEN_DAYS: '/system/handbook/nextSevenDays',
    STUDENT_HANDBOOK_STUDENTS: '/system/handbook/students',
    SWITCH_STUDENT: '/system/handbook/switchStudent',
    NOTICE_LIST: '/system/notice/list',
    NOTICE_DETAIL: '/system/notice',
    NOTICE_MARK_READ: '/system/notice',  // POST /{id}/read
    NOTICE_UNREAD_COUNT: '/system/notice/unreadCount',  // GET 获取未读通知数量
    VALIDATE_TOKEN: '/system/token/validate',             // GET 校验当前 token 是否有效
    FILE_UPLOAD: '/common/upload'                         // POST 文件上传
};

// 获取基础URL - 统一使用 /sp-api 前缀，匹配 Nginx 与 Vite 的 proxy 设定
const getBaseURL = () => {
    return '/sp-api';
};

// 构建完整的API端点URL
const API_ENDPOINTS = {};
const baseURL = getBaseURL();
for (const [key, value] of Object.entries(apiEndpoints)) {
    // 确保value以/开头，baseURL不以/结尾
    const path = value.startsWith('/') ? value : '/' + value;
    API_ENDPOINTS[key] = baseURL + path;
}

// 导出单独的BASE_URL，便于其他地方使用
export { baseURL };

export {API_ENDPOINTS};

export default {
    API_ENDPOINTS
};
// API 配置文件

// 定義通用的API端點路徑
const apiEndpoints = {
    STUDENT_HANDBOOK_PAST_MONTH: '/system/handbook/pastMonth',
    STUDENT_HANDBOOK_TODAY: '/system/handbook/today',
    STUDENT_HANDBOOK_NEXT_SEVEN_DAYS: '/system/handbook/nextSevenDays',
    STUDENT_HANDBOOK_STUDENTS: '/system/handbook/students',
    STUDENT_PHOTO: '/system/handbook/photo',
    SWITCH_STUDENT: '/system/handbook/switchStudent',
    NOTICE_LIST: '/system/notice/list',
    NOTICE_DETAIL: '/system/notice',
    NOTICE_MARK_READ: '/system/notice',  // POST /{id}/read
    NOTICE_UNREAD_COUNT: '/system/notice/unreadCount',  // GET 獲取未讀通知數量
    VALIDATE_TOKEN: '/system/token/validate',             // GET 校驗當前 token 是否有效
    FILE_UPLOAD: '/common/upload',                         // POST 文件上傳
    CALENDAR_LIST: '/system/calendar/list'                // 獲取行事曆列表
};

// 獲取基礎URL - 統一使用 /sp-api 前綴，匹配 Nginx 與 Vite 的 proxy 設定
const getBaseURL = () => {
    return '/sp-api';
};

// 構建完整的API端點URL
const API_ENDPOINTS = {};
const baseURL = getBaseURL();
for (const [key, value] of Object.entries(apiEndpoints)) {
    // 確保value以/開頭，baseURL不以/結尾
    const path = value.startsWith('/') ? value : '/' + value;
    API_ENDPOINTS[key] = baseURL + path;
}

// 導出單獨的BASE_URL，便於其他地方使用
export { baseURL };

export {API_ENDPOINTS};
// API 配置文件

// 定义通用的API端点路径
const apiEndpoints = {
    STUDENT_HANDBOOK_LIST: '/system/handbook/list',
    STUDENT_HANDBOOK_PAST_MONTH: '/system/handbook/pastMonth',
    STUDENT_HANDBOOK_TODAY: '/system/handbook/today',
    STUDENT_HANDBOOK_NEXT_SEVEN_DAYS: '/system/handbook/nextSevenDays',
    STUDENT_HANDBOOK_STUDENTS: '/system/handbook/students',
    SWITCH_STUDENT: '/system/handbook/switchStudent'
};

// 获取基础URL - 从环境变量读取或者使用默认值
const getBaseURL = () => {
    const envBaseUrl = import.meta.env.VITE_API_BASE_URL;
    if (envBaseUrl !== undefined && envBaseUrl !== null && envBaseUrl !== '') {
        // 确保URL末尾没有斜杠
        return envBaseUrl.endsWith('/') ? envBaseUrl.slice(0, -1) : envBaseUrl;
    }
    // 默认值 - 使用/api前缀用于开发环境，生产环境使用/sp-api前缀
    return import.meta.env.MODE === 'production' ? '/sp-api' : '/api';
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
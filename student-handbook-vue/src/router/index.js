import {createRouter, createWebHistory} from 'vue-router'
import Home from '../views/Home.vue'
import StudentHandbook from '../views/StudentHandbook.vue'
import Login from '../views/Login.vue'
import ParentNotice from '../views/ParentNotice.vue'
import NoticeDetail from '../views/NoticeDetail.vue'
import {stripAuthParamsFromUrl, saveTokenFromUrl} from '@/utils/wechat.js'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: Home
    },
    {
        path: '/handbook',
        name: 'StudentHandbook',
        component: StudentHandbook
    },
    {
        path: '/login',
        name: 'Login',
        component: Login
    },
    {
        path: '/notice',
        name: 'ParentNotice',
        component: ParentNotice
    },
    {
        path: '/notice/:id',
        name: 'NoticeDetail',
        component: NoticeDetail,
        props: true
    },
    {
        path: '/calendar',
        name: 'Calendar',
        component: () => import('../views/Calendar.vue')
    },
    {
        path: '/attendance',
        name: 'AttendanceRecord',
        component: () => import('../views/AttendanceRecord.vue')
    }
]

const router = createRouter({
    history: createWebHistory('/'),
    routes,
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) {
            return savedPosition
        } else {
            return { top: 0 }
        }
    }
})

router.afterEach((to, from) => {
    if (to.meta) {
        to.meta.fromPath = from.path
    } else {
        to.meta = { fromPath: from.path }
    }
    // 清掉地址欄殘留的 token，避免微信複製/轉發鏈接洩露登錄態
    stripAuthParamsFromUrl()
    // 非首頁不保留「新登錄」標記，避免之後進首頁誤彈「登錄成功」
    if (to.path !== '/') {
        saveTokenFromUrl.lastWasNewLogin = false
    }
})

export default router

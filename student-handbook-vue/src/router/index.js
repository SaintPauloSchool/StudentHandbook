import {createRouter, createWebHistory} from 'vue-router'
import Home from '../views/Home.vue'
import StudentHandbook from '../views/StudentHandbook.vue'
import Login from '../views/Login.vue'
import ParentNotice from '../views/ParentNotice.vue'
import NoticeDetail from '../views/NoticeDetail.vue'

// Nginx 會把 /20260617115111/ 轉給前端，瀏覽器 URL 帶版本前綴，Router base 需自動識別
function detectVersionBase() {
    const m = window.location.pathname.match(/^\/(\d{14})(?=\/|$)/)
    return m ? `/${m[1]}/` : '/'
}

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
    }
]

const router = createRouter({
    history: createWebHistory(detectVersionBase()),
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
})

export default router
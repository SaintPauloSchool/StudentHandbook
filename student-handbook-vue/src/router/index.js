import {createRouter, createWebHistory} from 'vue-router'
import { VERSION_KEY, RELOAD_FLAG } from '@/utils/versionCheck'
import Home from '../views/Home.vue'
import StudentHandbook from '../views/StudentHandbook.vue'
import Login from '../views/Login.vue'
import ParentNotice from '../views/ParentNotice.vue'
import NoticeDetail from '../views/NoticeDetail.vue'

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

// SPA 內跳轉時保持 ?_v=，避免微信按路徑緩存不帶版本號的 URL
router.beforeEach((to, from, next) => {
    const storedV = localStorage.getItem(VERSION_KEY);
    if (storedV && to.query[RELOAD_FLAG] !== storedV) {
        next({ ...to, query: { ...to.query, [RELOAD_FLAG]: storedV }, replace: true });
        return;
    }
    next();
});

router.afterEach((to, from) => {
    if (to.meta) {
        to.meta.fromPath = from.path
    } else {
        to.meta = { fromPath: from.path }
    }
})

export default router
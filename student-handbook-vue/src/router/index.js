import {createRouter, createWebHistory} from 'vue-router'
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
    }
]

const router = createRouter({
    history: createWebHistory('/sp-api/'),
    routes,
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) {
            return savedPosition
        } else {
            return { top: 0 }
        }
    }
})

export default router
import {createRouter, createWebHistory} from 'vue-router'
import Home from '../views/Home.vue'
import StudentHandbook from '../components/StudentHandbook.vue'
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
    routes
})

export default router
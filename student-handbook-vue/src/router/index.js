import {createRouter, createWebHistory} from 'vue-router'
import Home from '../views/Home.vue'
import StudentHandbook from '../components/StudentHandbook.vue'
import Login from '../views/Login.vue'

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
    }
]

const router = createRouter({
    history: createWebHistory('/sp-api/'),
    routes
})

export default router
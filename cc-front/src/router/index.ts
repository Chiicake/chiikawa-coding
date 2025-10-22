import { createRouter, createWebHistory } from 'vue-router'
import NewHomePage from '@/pages/NewHomePage.vue'
import HomePage from '@/pages/HomePage.vue'
import MyAppsPage from '@/pages/MyAppsPage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import ACCESS_ENUM from '@/access/accessEnum'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/about',
      name: 'about',
      component: () => import('@/pages/AboutView.vue'),
    },
    {
      path: '/',
      name: '主页',
      component: NewHomePage,
    },
    {
      path: '/generate',
      name: '应用生成',
      component: HomePage,
    },
    {
      path: '/my-apps',
      name: '我的应用',
      component: MyAppsPage,
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
      meta: {
        access: ACCESS_ENUM.ADMIN,
      },
    },
    {
      path: '/app/chat/:id',
      name: '应用对话',
      component: () => import('@/pages/AppChatPage.vue'),
    },
    {
      path: '/app/edit/:id',
      name: '编辑应用',
      component: () => import('@/pages/AppEditPage.vue'),
    },
    {
      path: '/admin/appManage',
      name: '应用查询',
      component: () => import('@/pages/admin/AppManagePage.vue'),
      meta: {
        access: ACCESS_ENUM.ADMIN,
      },
    },
    {
      path: '/admin/chatManage',
      name: '对话管理',
      component: () => import('@/pages/admin/ChatManagePage.vue'),
      meta: {
        access: ACCESS_ENUM.ADMIN,
      },
    },
    {
      path: '/noAuth',
      name: '无权限',
      component: () => import('@/pages/noAuth.vue'),
    },
  ],
})

export default router
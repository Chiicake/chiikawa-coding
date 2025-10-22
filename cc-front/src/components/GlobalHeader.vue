<template>
  <a-layout-header class="header">
    <div class="header-content">
      <!-- Logo 和标题 -->
      <div class="logo-wrapper">
        <img v-if="logoUrl" :src="logoUrl" alt="Logo" class="logo" />
        <span v-else class="logo-placeholder">CC</span>
        <h1 class="site-title">{{ siteTitle }}</h1>
      </div>

      <!-- 导航菜单 -->
      <div class="menu-wrapper">
        <a-menu
          mode="horizontal"
          :selectedKeys="[currentPath]"
          :items="menuItems"
          class="menu"
          triggerSubMenuAction="hover"
          @select="handleMenuSelect"
        />
      </div>
      <div class="user-login-status">
        <div v-if="loginUserStore.loginUser.id">
          <a-dropdown>
            <a-space class="user-avatar-container">
              <a-avatar :src="loginUserStore.loginUser.userAvatar" />
              {{ loginUserStore.loginUser.userName ?? "Chiikawa" }}
              <a-icon type="down" />
            </a-space>
            <template #overlay>
              <a-menu @click="handleLogout">
                <a-menu-item key="logout">
                  <a-icon type="logout" />
                  <span>退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
        <div v-else>
          <a-button type="primary" href="/user/login">登录</a-button>
        </div>
      </div>

    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref, computed, h } from 'vue'
import { useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'
import { HomeOutlined } from '@ant-design/icons-vue'
import { userLogout } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import ACCESS_ENUM from '@/access/accessEnum'
// 导入图片资源
import iconImg from '@/assets/images/icon.png'

// 初始化状态和路由
const router = useRouter()
const loginUserStore = useLoginUserStore()
const siteTitle = ref('Chiikawa Coding')
const logoUrl = ref<string>(iconImg)

// 菜单配置项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/generate-menu',
    label: '应用',
    title: '应用',
    disabled: false,
    children: [
      {
        key: '/generate',
        label: '应用生成',
        title: '应用生成',
      },
      {
        key: '/my-apps',
        label: '我的应用',
        title: '我的应用',
      }
    ]
  },
  {
    key: "/admin/userManage",
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/appManage',
    label: '应用管理',
    title: '应用管理',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      const role = String(loginUser?.userRole || '').toLowerCase()
      if (!loginUser || role !== ACCESS_ENUM.ADMIN) {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

// 获取当前路径
const currentPath = computed(() => {
  return router.currentRoute.value.path
})

// 处理菜单选择
const handleMenuSelect = (selectedKeys: any) => {
  try {
    console.log('Menu selected keys type:', typeof selectedKeys)
    console.log('Menu selected keys value:', selectedKeys)
    
    // 处理不同类型的selectedKeys参数
    let path: string | undefined
    if (Array.isArray(selectedKeys) && selectedKeys.length > 0) {
      path = selectedKeys[0]
    } else if (typeof selectedKeys === 'string') {
      path = selectedKeys
    } else if (selectedKeys && typeof selectedKeys === 'object' && selectedKeys.key) {
      // 处理可能的对象格式，如 { key: '/path' }
      path = selectedKeys.key
    }
    
    console.log('Extracted path:', path)
    
    if (!path || typeof path !== 'string') {
      console.error('Invalid path (not a string or empty):', path)
      return
    }
    
    // 确保router已正确初始化
    if (!router || typeof router.push !== 'function') {
      console.error('Router not properly initialized:', router)
      return
    }
    
    // 安全地执行路由跳转
    router.push(path).then(() => {
      console.log('Navigation successful to:', path)
    }).catch(error => {
      console.error('Navigation error:', error)
    })
  } catch (error) {
    console.error('Error in handleMenuSelect:', error)
  }
}

// 处理登录
const handleLogin = () => {
  // 实际项目中应该跳转到登录页面
  console.log('登录按钮点击')
}

// 处理退出登录
const handleLogout = async () => {
  try {
    const res = await userLogout()
    if (res.data.code === 0) {
      // 清除登录状态
      await loginUserStore.logout()
      message.success('退出登录成功')
      // 跳转到登录页面
      router.push('/user/login')
    } else {
      message.error('退出登录失败: ' + res.data.message)
    }
  } catch (error) {
    message.error('退出登录失败，请稍后重试')
    console.error('退出登录错误:', error)
  }
}

</script>

<style scoped>
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 4px;
}

.logo-placeholder {
  width: 40px;
  height: 40px;
  background: #1890ff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  font-weight: bold;
  font-size: 18px;
}

.site-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.menu-wrapper {
  flex: 1;
  margin: 0 40px;
}

.menu {
  border-bottom: none;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar-container {
  padding: 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.user-avatar-container:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

:deep(.ant-dropdown-menu-item) {
  transition: background-color 0.3s;
}

:deep(.ant-dropdown-menu-item:hover) {
  background-color: #f5f5f5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 12px;
  }

  .site-title {
    display: none;
  }

  .menu-wrapper {
    margin: 0 16px;
  }

  .menu {
    min-width: auto;
  }

  .menu .ant-menu-item {
    padding: 0 8px;
    font-size: 14px;
  }
}
</style>







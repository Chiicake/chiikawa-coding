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
          class="menu"
          @select="handleMenuSelect"
        >
          <a-menu-item v-for="item in menuItems" :key="item.path">
            <router-link :to="item.path">
              {{ item.name }}
            </router-link>
          </a-menu-item>
        </a-menu>
      </div>

      <!-- 用户区域 -->
      <div class="user-area">
        <a-button type="primary" @click="handleLogin">登录</a-button>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'

// 菜单配置
const menuItems = ref([
  { path: '/', name: '首页' },
  { path: '/about', name: '关于' }
])

const router = useRouter()
const siteTitle = ref('Chiikawa Coding')
const logoUrl = ref<string>('src/assets/images/icon.png')

// 获取当前路径
const currentPath = computed(() => {
  return router.currentRoute.value.path
})

// 处理菜单选择
const handleMenuSelect = (selectedKeys: string[]) => {
  const path = selectedKeys[0]
  router.push(path)
}

// 处理登录
const handleLogin = () => {
  // 实际项目中应该跳转到登录页面
  console.log('登录按钮点击')
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

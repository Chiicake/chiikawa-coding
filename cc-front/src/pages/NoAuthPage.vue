<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Button } from 'ant-design-vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goLogin = () => {
  router.push('/user/login')
}
</script>

<template>
  <div class="no-auth-container">
    <div class="no-auth-content">
      <div class="no-auth-icon">🚫</div>
      <h1 class="no-auth-title">没有访问权限</h1>
      <p class="no-auth-description">
        您没有权限访问此页面，请返回首页或登录后重试。
      </p>
      <div class="no-auth-actions">
        <Button type="primary" @click="goHome" style="margin-right: 16px;">
          返回首页
        </Button>
        <Button @click="goLogin">
          去登录
        </Button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.no-auth-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 64px);
  background: #f0f2f5;
}

.no-auth-content {
  text-align: center;
  padding: 48px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 90%;
}

.no-auth-icon {
  font-size: 72px;
  margin-bottom: 24px;
}

.no-auth-title {
  font-size: 28px;
  margin-bottom: 16px;
  color: #333;
}

.no-auth-description {
  font-size: 16px;
  color: #666;
  margin-bottom: 32px;
  line-height: 1.6;
}

.no-auth-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

@media (max-width: 768px) {
  .no-auth-content {
    padding: 32px 16px;
    width: 95%;
  }
  
  .no-auth-icon {
    font-size: 60px;
    margin-bottom: 16px;
  }
  
  .no-auth-title {
    font-size: 24px;
  }
  
  .no-auth-description {
    font-size: 14px;
  }
  
  .no-auth-actions {
    flex-direction: column;
    gap: 12px;
  }
  
  .no-auth-actions :deep(.ant-btn) {
    width: 100%;
  }
}
</style>
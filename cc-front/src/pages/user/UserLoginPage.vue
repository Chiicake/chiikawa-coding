<script setup lang="ts">
import { userLogin } from '@/api/userController.ts'
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { message } from 'ant-design-vue'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})
const router = useRouter()
const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const res = await userLogin(values)
  // 登录成功，把登录态保存到全局状态中
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功！欢迎！🎉')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败: ' + res.data.message)
  }
}
</script>

<template>
  <div id="userLoginPage">
    <h2 class="title">Chiikawa 编程 - 登录</h2>
    <div class="subtitle">Chiikawa Coding - Login</div>
    <div class="desc">欢迎来到可爱的编程冒险！</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入用户名' }]">
        <a-input v-model:value="formState.userAccount" placeholder="用户名" />
      </a-form-item>
      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码至少8个字符' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="密码" />
      </a-form-item>
      <div class="tips">
        还没有账号？
        <RouterLink to="/user/register">立即注册</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
      </a-form-item>
    </a-form>
    <div class="chiikawa-message">
      「和ちいかわ一起快乐编程吧！」🐾
    </div>
  </div>
</template>

<style scoped>
/* 样式部分保持不变 */
#userLoginPage {
  max-width: 400px;
  margin: 0 auto;
  padding: 40px 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.15);
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.title {
  text-align: center;
  margin-bottom: 8px;
  font-size: 24px;
  color: #ff6b6b;
  font-weight: bold;
  letter-spacing: 1px;
}

.subtitle {
  text-align: center;
  margin-bottom: 20px;
  font-size: 16px;
  color: #4ecdc4;
  font-weight: 500;
}

.desc {
  text-align: center;
  color: #6a5acd;
  margin-bottom: 30px;
  font-size: 14px;
  font-weight: 500;
}

.tips {
  margin-bottom: 20px;
  color: #6c757d;
  font-size: 13px;
  text-align: right;
}

.chiikawa-message {
  text-align: center;
  margin-top: 20px;
  color: #ff9f1c;
  font-size: 14px;
  font-weight: 500;
  font-style: italic;
}

/* 为表单元素添加可爱风格 */
:deep(.ant-input),
:deep(.ant-input-password) {
  border-radius: 12px;
  border-color: #ffe66d;
  transition: all 0.3s ease;
}

:deep(.ant-input:focus),
:deep(.ant-input-password:focus-within) {
  border-color: #ff6b6b;
  box-shadow: 0 0 0 2px rgba(255, 107, 107, 0.2);
}

:deep(.ant-btn-primary) {
  background-color: #ff6b6b;
  border-color: #ff6b6b;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.ant-btn-primary:hover),
:deep(.ant-btn-primary:focus) {
  background-color: #ff5252;
  border-color: #ff5252;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}

:deep(.ant-btn-primary:active) {
  transform: translateY(0);
}
</style>

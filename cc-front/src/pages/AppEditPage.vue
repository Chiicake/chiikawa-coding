<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppById, updateApp, updateAppForAdmin } from '@/api/appController'
// 移除不存在的类型导入
import { useLoginUserStore } from '@/stores/loginUser'
import { Form, Input, Button, Card, Upload, InputNumber, Typography, Space } from 'ant-design-vue'
import type { UploadFile, UploadProps } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const appId = ref('')
const app = ref<any | null>(null)
const loading = ref(true)
const formLoading = ref(false)
const isAdmin = ref(false)
const isOwnApp = ref(false)

// 表单数据
const formData = ref({
  appName: '',
  priority: 0
})

// 封面图片
const coverImage = ref<UploadFile[]>([])

// 获取应用信息
const fetchAppInfo = async () => {
  if (!appId.value) return
  
  try {
    const response = await getAppById({ id: appId.value })
    if (response.data.code === 0) {
      app.value = response.data.data
      formData.value.appName = app.value.appName || ''
      formData.value.priority = app.value.priority || 0
      
      // 设置封面图片
      if (app.value.cover) {
        coverImage.value = [{
          uid: '-1',
          name: 'cover.jpg',
          status: 'done',
          url: app.value.cover
        }]
      }
      
      // 检查是否是自己的应用
      isOwnApp.value = loginUserStore.loginUser?.id === app.value.userId
      
      // 检查权限
      checkPermission()
    } else {
      message.error(response.data.message || '获取应用信息失败')
      router.push('/')
    }
  } catch (error) {
    message.error('获取应用信息失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 检查权限
const checkPermission = () => {
  // 管理员可以编辑任意应用
  // 改用userRole属性检查管理员权限
  isAdmin.value = loginUserStore.loginUser?.userRole === 'ADMIN' || false
  
  // 普通用户只能编辑自己的应用
  if (!isAdmin.value && !isOwnApp.value) {
    message.error('没有权限编辑此应用')
    router.push('/noAuth')
    return false
  }
  return true
}

// 提交表单
const handleSubmit = async () => {
  if (!formData.value.appName.trim()) {
    message.warning('应用名称不能为空')
    return
  }
  
  formLoading.value = true
  try {
    // 非管理员：只能更新名称
    if (!isAdmin.value) {
      const response = await updateApp({
        id: appId.value,
        appName: formData.value.appName.trim()
      })
      if (response.data.code === 0) {
        message.success('更新成功')
        router.push(`/app/chat/${appId.value}`)
      } else {
        message.error(response.data.message || '更新失败')
      }
    } else {
      // 管理员：可更新名称、优先级、封面
      const adminUpdateBody: any = {
        id: appId.value,
        appName: formData.value.appName.trim(),
        priority: formData.value.priority
      }
      if (coverImage.value[0]?.url) {
        adminUpdateBody.cover = coverImage.value[0].url
      }
      const response = await updateAppForAdmin(adminUpdateBody)
      if (response.data.code === 0) {
        message.success('更新成功')
        router.push(`/app/chat/${appId.value}`)
      } else {
        message.error(response.data.message || '更新失败')
      }
    }
  } catch (error) {
    message.error('更新失败')
  } finally {
    formLoading.value = false
  }
}

// 取消操作
const handleCancel = () => {
  router.push(`/app/chat/${appId.value}`)
}

// 上传前检查
const beforeUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    message.error('只支持 JPG 或 PNG 格式')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

// 上传变化处理
const handleUploadChange = (info: any) => {
  if (info.file.status === 'done') {
    // 上传成功后处理返回的URL
    const response = info.file.response
    if (response && response.code === 0) {
      coverImage.value = [{
        uid: info.file.uid,
        name: info.file.name,
        status: 'done',
        url: response.data.url
      }]
    }
  } else if (info.file.status === 'error') {
    message.error('上传失败')
  }
}

// 预览图片
const handlePreview = (file: UploadFile) => {
  window.open(file.url as string, '_blank')
}

onMounted(async () => {
  appId.value = route.params.id as string
  await loginUserStore.fetchLoginUser()
  await fetchAppInfo()
})
</script>

<template>
  <div class="app-edit-container">
    <div class="page-header">
      <Typography.Title :level="4">编辑应用信息</Typography.Title>
    </div>
    
    <div v-if="loading" class="loading-container">
      <Card loading />
    </div>
    
    <Card v-else class="edit-form-card">
      <Form layout="vertical">
        <!-- 应用名称 -->
        <Form.Item 
          label="应用名称" 
          name="appName"
          :rules="[{ required: true, message: '请输入应用名称' }]"
        >
          <Input 
            v-model:value="formData.appName" 
            placeholder="请输入应用名称"
            :maxlength="50"
            show-count
          />
        </Form.Item>
        
        <!-- 封面图片（仅管理员可见） -->
        <Form.Item 
          v-if="isAdmin" 
          label="应用封面"
        >
          <Upload 
            v-model:fileList="coverImage" 
            :multiple="false"
            name="file"
            action="/api/upload"
            list-type="picture-card"
            :max-count="1"
            :before-upload="beforeUpload"
            :on-change="handleUploadChange"
            :on-preview="handlePreview"
          >
            <div v-if="coverImage.length < 1">
              <div class="ant-upload-icon">+</div>
              <div class="ant-upload-text">上传封面</div>
            </div>
          </Upload>
          <Typography.Text type="secondary">
            建议上传 1200x600 的 JPG 或 PNG 图片，大小不超过 2MB
          </Typography.Text>
        </Form.Item>
        
        <!-- 优先级（仅管理员可见） -->
        <Form.Item 
          v-if="isAdmin" 
          label="优先级"
        >
          <InputNumber
            v-model:value="formData.priority"
            :min="0"
            :max="99"
            style="width: 100%"
            placeholder="优先级，99表示精选"
          />
          <Typography.Text type="secondary">
            设置为 99 表示将该应用设为精选
          </Typography.Text>
        </Form.Item>
        
        <!-- 操作按钮 -->
        <Form.Item>
          <Space>
            <Button 
              type="primary" 
              @click="handleSubmit"
              :loading="formLoading"
            >
              保存
            </Button>
            <Button @click="handleCancel">
              取消
            </Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
    
    <!-- 应用信息卡片 -->
    <Card v-if="!loading && app" class="app-info-card">
      <Typography.Title :level="5">应用信息</Typography.Title>
      <div class="info-item">
        <Typography.Text strong>应用ID：</Typography.Text>
        <Typography.Text copyable>{{ app.id }}</Typography.Text>
      </div>
      <div class="info-item">
        <Typography.Text strong>创建用户：</Typography.Text>
        <Typography.Text>{{ app.user?.username || '未知用户' }}</Typography.Text>
      </div>
      <div class="info-item">
        <Typography.Text strong>创建时间：</Typography.Text>
        <Typography.Text>{{ new Date(app.createTime || Date.now()).toLocaleString() }}</Typography.Text>
      </div>
      <div class="info-item">
        <Typography.Text strong>部署状态：</Typography.Text>
        <Typography.Text>{{ app.deployUrl ? '已部署' : '未部署' }}</Typography.Text>
      </div>
      <div v-if="app.deployUrl" class="info-item">
        <Typography.Text strong>部署地址：</Typography.Text>
        <Typography.Link :href="app.deployUrl" target="_blank">
          {{ app.deployUrl }}
        </Typography.Link>
      </div>
    </Card>
  </div>
</template>

<style scoped>
.app-edit-container {
  padding: 24px;
  background: #f5f5f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 24px;
}

.edit-form-card,
.app-info-card {
  margin-bottom: 24px;
}

.loading-container {
  margin-bottom: 24px;
}

.info-item {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item:last-child {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .app-edit-container {
    padding: 16px;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>
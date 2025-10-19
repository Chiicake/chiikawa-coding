<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppById, deployApp, updateApp } from '@/api/appController'
// 移除不存在的导入
// 移除不存在的类型导入
import { useLoginUserStore } from '@/stores/loginUser'
import { Button, Input, Card, Layout, Tabs, Modal, notification } from 'ant-design-vue'
import type { TabsProps } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const appId = ref('')
const app = ref<any | null>(null)
const loading = ref(true)
const messages = ref<any[]>([])
const newMessage = ref('')
const isSending = ref(false)
const isDeploying = ref(false)
const deploymentUrl = ref('')
const showDeployModal = ref(false)
const showUpdateModal = ref(false)
const newAppName = ref('')
const generatingCompleted = ref(false)
const showWebPreview = ref(false)
const previewCollapsed = ref(false)



// 获取应用信息
const fetchAppInfo = async () => {
  if (!appId.value) return
  
  try {
    const response = await getAppById({ id: appId.value })
    if (response.data.code === 0) {
      app.value = response.data.data
      newAppName.value = app.value.appName || ''
      // 如果已有部署URL，直接显示预览
      if (app.value.deployUrl) {
        deploymentUrl.value = app.value.deployUrl
        showWebPreview.value = true
      }
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

// 发送消息
const handleSendMessage = async () => {
  if (!newMessage.value.trim() || isSending.value) return
  
  const userMessage = newMessage.value.trim()
  newMessage.value = ''
  
  // 添加用户消息到界面
  const userMsgId = `msg_${Date.now()}_user`
  messages.value.push({ role: 'user', content: userMessage, id: userMsgId })
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  isSending.value = true
  
  try {
    // 创建AI消息占位符
    const aiMsgId = `msg_${Date.now()}_ai`
    messages.value.push({ role: 'ai', content: '', id: aiMsgId })
    
    // 初始化AI消息索引
    let aiMessageIndex = messages.value.length - 1
    
    // 创建EventSource连接到SSE端点，使用完整的API基础URL
    const eventSource = new EventSource(
      'http://localhost:8123/api/app/chat/gen/code?appId=' + appId.value + '&message=' + encodeURIComponent(userMessage),
      { withCredentials: true }
    )
    
    // 处理接收到的SSE数据
    eventSource.onmessage = (event) => {
      try {
        // 解析SSE消息数据（格式：data:{"d":"字符"}）
        const data = JSON.parse(event.data)
        
        // 提取字符内容
        if (data && typeof data.d === 'string') {
          // 将字符追加到消息内容
          messages.value[aiMessageIndex].content += data.d
          
          // 每次更新后滚动到底部
          nextTick(() => {
            scrollToBottom()
          })
        }
      } catch (parseError) {
        console.error('SSE消息解析错误:', parseError)
        console.log('原始消息:', event.data)
        // 如果解析失败，记录错误但不影响流程
      }
    }
    
    // 处理自定义事件，特别是done事件
    eventSource.addEventListener('done', () => {
      console.log('收到SSE结束事件')
      generatingCompleted.value = true
      showWebPreview.value = true
      
      // 关闭EventSource连接
      eventSource.close()
      isSending.value = false
      
      // 提示用户生成完成
      notification.success({
        message: '网站生成完成',
        description: '右侧预览已准备就绪，您可以查看效果或进行部署',
        duration: 5
      })
    })
    
    // 处理连接错误
    eventSource.onerror = (error) => {
      console.error('SSE连接错误:', error)
      // 只有在isSending为true且generatingCompleted为false时才认为是真正的错误
      if (isSending.value && !generatingCompleted.value) {
        isSending.value = false
        eventSource.close()
        
        // 更新消息显示错误信息
        messages.value[aiMessageIndex].content += '\n\n连接错误，请重试'
        message.error('连接服务器失败')
      }
    }
    
    // 处理连接关闭
    eventSource.onclose = () => {
      console.log('SSE连接已关闭')
      // 确保重置发送状态
      if (isSending.value) {
        isSending.value = false
        // 如果是意外关闭且没有设置生成完成标志，也设置预览
        if (!generatingCompleted.value) {
          generatingCompleted.value = true
          showWebPreview.value = true
        }
      }
    }
    
  } catch (error) {
    isSending.value = false
    message.error('发送消息失败')
  }
}

// 部署应用
const handleDeploy = async () => {
  if (isDeploying.value) return
  
  isDeploying.value = true
  showDeployModal.value = true
  
  try {
    const response = await deployApp({ appId: appId.value })
    if (response.data.code === 0) {
      deploymentUrl.value = response.data.data || ''
      app.value!.deployUrl = deploymentUrl.value
      message.success('部署成功')
      
      // 更新应用信息
      await updateApp({
        id: appId.value,
        deployUrl: deploymentUrl.value
      })
    } else {
      message.error(response.data.message || '部署失败')
    }
  } catch (error) {
    message.error('部署失败')
  } finally {
    isDeploying.value = false
  }
}

// 更新应用名称
const handleUpdateAppName = async () => {
  if (!newAppName.value.trim()) {
    message.warning('应用名称不能为空')
    return
  }
  
  try {
    const response = await updateApp({
      id: appId.value,
      appName: newAppName.value.trim()
    })
    if (response.data.code === 0) {
      app.value!.appName = newAppName.value.trim()
      showUpdateModal.value = false
      message.success('更新成功')
    } else {
      message.error(response.data.message || '更新失败')
    }
  } catch (error) {
    message.error('更新失败')
  }
}

// 复制部署URL
const copyDeployUrl = () => {
  if (deploymentUrl.value) {
    navigator.clipboard.writeText(deploymentUrl.value)
      .then(() => message.success('URL已复制到剪贴板'))
      .catch(() => message.error('复制失败'))
  }
}

// 滚动到消息底部
const scrollToBottom = () => {
  const messageContainer = document.querySelector('.message-container')
  if (messageContainer) {
    messageContainer.scrollTop = messageContainer.scrollHeight
  }
}

// 检查用户权限
const checkUserPermission = () => {
  if (!loginUserStore.loginUser) {
    router.push('/user/login')
    return false
  }
  return true
}

// 生成静态文件URL（与后端约定：/api/static/{codeGenType}_{appId}/index.html）
const getStaticFileUrl = () => {
  const codeGenType = app.value?.codeGenType || 'html'
  const deployKey = `${codeGenType}_${appId.value}`
  // 可以使用带斜杠的目录URL，后端会重定向到 index.html；这里直接指向 index.html
  return `http://localhost:8123/api/static/${deployKey}/index.html`
}

// 页面初始化
onMounted(async () => {
  appId.value = route.params.id as string
  
  if (!checkUserPermission()) return
  
  await fetchAppInfo()
  
  // 如果是新创建的应用，自动发送初始提示词
  if (app.value && app.value.initPrompt && messages.value.length === 0) {
    newMessage.value = app.value.initPrompt
    handleSendMessage()
  }
})

// 监听路由参数变化
watch(() => route.params.id, (newId) => {
  if (newId && newId !== appId.value) {
    appId.value = newId as string
    fetchAppInfo()
    messages.value = []
    generatingCompleted.value = false
    showWebPreview.value = false
  }
})
</script>

<template>
  <div v-if="loading" class="loading-container">
    <Card loading />
  </div>
  
  <div v-else-if="app" class="app-chat-container">
    <!-- 顶部栏 -->
    <div class="app-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="app-title">{{ app.appName || '未命名应用' }}</h1>
        </div>
        <div class="header-right">
          <Button 
    type="link" 
    @click="showUpdateModal = true"
    style="margin-right: 8px; min-width: auto; padding: 4px 12px;"
  >
    编辑名称
  </Button>
          <Button 
            type="primary" 
            @click="handleDeploy"
            :loading="isDeploying"
            v-if="generatingCompleted"
          >
            {{ deploymentUrl ? '重新部署' : '部署应用' }}
          </Button>
          <Button
            type="link"
            v-if="showWebPreview"
            @click="previewCollapsed = !previewCollapsed"
            style="min-width: auto; padding: 4px 12px;"
          >
            {{ previewCollapsed ? '显示预览' : '隐藏预览' }}
          </Button>
        </div>
      </div>
    </div>
    
    <!-- 核心内容区域 -->
    <div class="app-content">
      <div class="content-layout">
        <!-- 左侧对话区域 -->
        <div class="chat-sider">
          <div class="message-container">
            <div 
              v-for="msg in messages" 
              :key="msg.id"
              :class="['message-wrapper', msg.role]"
            >
              <div class="message-avatar">
                {{ msg.role === 'user' ? '👤' : '🤖' }}
              </div>
              <div class="message-content">
                <pre class="message-text">{{ msg.content }}</pre>
              </div>
            </div>
            
            <div v-if="messages.length === 0" class="empty-messages">
              <p>开始与AI对话来生成您的网站</p>
            </div>
          </div>
          
          <!-- 消息输入框 -->
          <div class="message-input-wrapper">
            <Input.TextArea
              v-model:value="newMessage"
              :placeholder="'输入您的想法，让AI帮您改进网站...'"
              :rows="3"
              show-count
              :max-length="1000"
              @keyup.enter.ctrl="handleSendMessage"
              :disabled="isSending"
              style="flex: 1;"
            />
            <Button 
              type="primary" 
              @click="handleSendMessage"
              :loading="isSending"
              class="send-button"
            >
              {{ isSending ? '发送中...' : '发送' }}
            </Button>
          </div>
        </div>
        
        <!-- 右侧预览区域 -->

        <div class="preview-sider" v-if="showWebPreview && !previewCollapsed">
          <Tabs 
            default-active-key="preview" 
            type="card"
            :style="{ height: '100%' }"
          >
            <Tabs.TabPane tab="网站预览" key="preview">
              <div class="web-preview-container">
                <iframe 
                  :src="getStaticFileUrl()" 
                  frameborder="0" 
                  class="web-preview"
                  sandbox="allow-scripts allow-same-origin allow-popups allow-forms"
                ></iframe>
              </div>
            </Tabs.TabPane>
            
            <Tabs.TabPane tab="部署信息" key="deploy" v-if="deploymentUrl">
              <div class="deploy-info">
                <p class="deploy-label">访问地址：</p>
                <div class="deploy-url-wrapper">
                  <Input.Text 
                    :value="deploymentUrl" 
                    readonly 
                    class="deploy-url"
                  />
                  <Button @click="copyDeployUrl" type="link">
                    复制
                  </Button>
                </div>
              </div>
            </Tabs.TabPane>
          </Tabs>
        </div>
        <div class="preview-sider" v-else>
          <div class="preview-placeholder">
            <p v-if="!showWebPreview && messages.length === 0">
              输入提示词，AI将为您生成网站
            </p>
            <p v-else-if="!showWebPreview">
              AI正在为您生成网站，请稍候...
            </p>
            <p v-else>
              预览已隐藏
            </p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 更新应用名称弹窗 -->
    <Modal
      v-model:open="showUpdateModal"
      title="更新应用名称"
      @ok="handleUpdateAppName"
      @cancel="showUpdateModal = false"
    >
      <Input v-model:value="newAppName" placeholder="请输入新的应用名称" />
    </Modal>
    
    <!-- 部署弹窗 -->
    <Modal
      v-model:open="showDeployModal"
      title="部署中"
      :footer="null"
      :closable="isDeploying === false"
      :maskClosable="false"
    >
      <div class="deploy-modal-content">
        <p v-if="isDeploying">正在为您部署应用，请稍候...</p>
        <div v-else-if="deploymentUrl" class="deploy-success">
          <p>部署成功！</p>
          <p class="deploy-success-url">
            <a :href="deploymentUrl" target="_blank">{{ deploymentUrl }}</a>
          </p>
        </div>
      </div>
    </Modal>
  </div>
</template>

<style scoped>
/* 全局重置，确保页面占满整个视口 */
* {
  box-sizing: border-box;
}

body, html {
  margin: 0;
  padding: 0;
  height: 100%;
  overflow: hidden;
}

.app-chat-container {
  height: 90vh;
  width: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.app-header {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  height: 64px;
  z-index: 10;
  flex-shrink: 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 80%;
  max-width: 1200px; /* 限制最大宽度，使内容更加紧凑 */
}

.app-title {
  font-size: 20px;
  margin: 0;
  color: #1890ff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-content {
  flex: 1;
  width: 100%;
  overflow: hidden;
  padding: 20px;
  display: flex;
  justify-content: center;
  background: #f5f5f5;
}

/* 核心布局 - 左右分栏 */
.content-layout {
  height: 100%;
  width: 100%;
  max-width: 1400px;
  display: flex;
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 左侧对话区域 - 调整为更合适的大小 */
.chat-sider {
  flex: 1;
  display: flex;
  width: 30%;
  flex-direction: column;
  background: #fff;
  border-right: 1px solid #f0f0f0;
  overflow: hidden;
}

/* 右侧预览区域 - 减小尺寸 */
.preview-sider {
  width: 32%; /* 原为40%，适当缩窄 */
  display: flex;
  flex-direction: column;
  background: #fff;
  border-left: 1px solid #f0f0f0;
  overflow: hidden;
}

/* 消息容器 - 可滚动区域 */
.message-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  /* 美化滚动条 */
  scrollbar-width: thin;
  scrollbar-color: #d9d9d9 #f5f5f5;
}

/* Webkit滚动条样式 */
.message-container::-webkit-scrollbar {
  width: 6px;
}

.message-container::-webkit-scrollbar-track {
  background: #f5f5f5;
}

.message-container::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.message-container::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}

/* 消息样式 - 减小对话框大小 */
.message-wrapper {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  margin-bottom: 4px;
}

.message-wrapper.user {
  flex-direction: row-reverse;
}

.message-wrapper.ai {
  flex-direction: row;
}

.message-avatar {
  font-size: 20px;
  flex-shrink: 0;
  margin-top: 2px;
}

.message-content {
  max-width: 70%;
  word-wrap: break-word;
  flex-shrink: 0;
}

.message-wrapper.user .message-content {
  text-align: right;
}

.message-text {
  background: #f0f0f0;
  padding: 8px 12px;
  border-radius: 6px;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  line-height: 1.4;
  font-size: 14px;
}

.message-wrapper.user .message-text {
  background: #1890ff;
  color: #fff;
}

.empty-messages {
  text-align: center;
  color: #999;
  margin-top: 48px;
  font-size: 16px;
}

/* 输入区域 */
.message-input-wrapper {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
  flex-shrink: 0;
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.send-button {
  margin-top: 0;
  float: none;
}

/* 让Tabs在预览侧充满高度 */
.preview-sider :deep(.ant-tabs) {
  height: 100%;
}
.preview-sider :deep(.ant-tabs-content-holder) {
  height: 100%;
}
.preview-sider :deep(.ant-tabs-content) {
  height: 100%;
}
.preview-sider :deep(.ant-tabs-tabpane) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 预览容器与iframe填满高度 */
.web-preview-container {
  flex: 1;
  display: flex;
  height: 100%;
}
.web-preview {
  width: 100%;
  height: 100%;
  border: 0;
}
</style>



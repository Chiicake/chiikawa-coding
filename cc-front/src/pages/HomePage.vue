<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { addApp, listUserAppVoByPage, listFeaturedAppVoByPage } from '@/api/appController'
// 移除不存在的类型导入
import { useLoginUserStore } from '@/stores/loginUser'
import { Button, Input, Card, Row, Col, Typography, Empty, Spin } from 'ant-design-vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const promptText = ref('')
const isCreating = ref(false)
const userApps = ref<any[]>([])
const featuredApps = ref<any[]>([])
const userAppsLoading = ref(false)
const featuredAppsLoading = ref(false)
const userPageNum = ref(1)
const featuredPageNum = ref(1)
const hasMoreUserApps = ref(true)
const hasMoreFeaturedApps = ref(true)
const appNameSearch = ref('')

// 创建应用
const handleCreateApp = async () => {
  console.log('开始创建应用，提示词:', promptText.value)
  if (!promptText.value.trim()) {
    message.warning('请输入提示词')
    console.log('提示词为空，创建应用失败')
    return
  }
  
  if (!loginUserStore.loginUser) {
    console.log('用户未登录，跳转到登录页')
    router.push('/user/login')
    return
  }
  
  isCreating.value = true
  try {
    console.log('调用addApp API创建应用')
    const response = await addApp({
      initPrompt: promptText.value
    })
    console.log('API返回结果:', response)
    
    if (response && response.data) {
      if (response.data.code === 0) {
        const appId = response.data.data
        console.log('应用创建成功，appId:', appId)
        console.log('尝试跳转到:', `/app/chat/${appId}`)
        router.push(`/app/chat/${appId}`)
      } else {
        const errorMsg = response.data.message || '创建应用失败'
        console.error('API返回错误:', errorMsg)
        message.error(errorMsg)
      }
    } else {
      console.error('API返回格式错误:', response)
      message.error('服务器返回格式错误')
    }
  } catch (error) {
    console.error('创建应用时发生异常:', error)
    message.error('创建应用失败: ' + (error instanceof Error ? error.message : String(error)))
  } finally {
    isCreating.value = false
  }
}

// 获取用户应用列表
const fetchUserApps = async (pageNum: number = 1, isLoadMore: boolean = false) => {
  if (userAppsLoading.value || (!isLoadMore && !hasMoreUserApps.value)) return
  
  userAppsLoading.value = true
  try {
    const queryParams = {
      pageNum,
      pageSize: 9,
      appName: appNameSearch.value
    }
    
    const response = await listUserAppVoByPage(queryParams)
    if (response.data.code === 0) {
      const newApps = response.data.data?.records || []
      if (isLoadMore) {
        userApps.value = [...userApps.value, ...newApps]
      } else {
        userApps.value = newApps
      }
      hasMoreUserApps.value = newApps.length === 9
    }
  } catch (error) {
    message.error('获取应用列表失败')
  } finally {
    userAppsLoading.value = false
  }
}

// 获取精选应用列表
const fetchFeaturedApps = async (pageNum: number = 1, isLoadMore: boolean = false) => {
  if (featuredAppsLoading.value || (!isLoadMore && !hasMoreFeaturedApps.value)) return
  
  featuredAppsLoading.value = true
  try {
    const queryParams = {
      pageNum,
      pageSize: 9,
      isFeatured: true,
      appName: appNameSearch.value
    }
    
    const response = await listFeaturedAppVoByPage(queryParams)
    if (response.data.code === 0) {
      const newApps = response.data.data?.records || []
      if (isLoadMore) {
        featuredApps.value = [...featuredApps.value, ...newApps]
      } else {
        featuredApps.value = newApps
      }
      hasMoreFeaturedApps.value = newApps.length === 9
    }
  } catch (error) {
    message.error('获取精选应用失败')
  } finally {
    featuredAppsLoading.value = false
  }
}

// 加载更多用户应用
const loadMoreUserApps = () => {
  if (!userAppsLoading.value && hasMoreUserApps.value) {
    userPageNum.value++
    fetchUserApps(userPageNum.value, true)
  }
}

// 加载更多精选应用
const loadMoreFeaturedApps = () => {
  if (!featuredAppsLoading.value && hasMoreFeaturedApps.value) {
    featuredPageNum.value++
    fetchFeaturedApps(featuredPageNum.value, true)
  }
}

// 搜索应用
const handleSearch = () => {
  userPageNum.value = 1
  featuredPageNum.value = 1
  hasMoreUserApps.value = true
  hasMoreFeaturedApps.value = true
  fetchUserApps()
  fetchFeaturedApps()
}

// 点击应用卡片
const handleAppClick = (app: any) => {
  router.push(`/app/chat/${app.id}`)
}

// 处理查看对话按钮（携带 noAuto=1，避免自动初始发送）
const handleViewChat = (app: any) => {
  router.push(`/app/chat/${app.id}?noAuto=1`)
}

// 处理查看作品按钮：打开部署地址 localhost/{deployKey}
const handleViewWork = (app: any) => {
  if (app.deployKey) {
    const url = `http://localhost:81/${app.deployKey}`
    window.open(url, '_blank')
  } else {
    message.warning('该应用尚未部署')
  }
}

// 获取默认应用封面
const getDefaultCover = () => {
  const covers = [
    'https://gw.alipayobjects.com/zos/rmsportal/JiqGstEfoWAOHiTxclqi.png',
    'https://gw.alipayobjects.com/zos/rmsportal/KDpgvguMpGfqaHPjicRK.svg',
    'https://gw.alipayobjects.com/zos/rmsportal/AngryPorcupine.svg'
  ]
  return covers[Math.floor(Math.random() * covers.length)]
}

onMounted(async () => {
  await loginUserStore.fetchLoginUser()
  if (loginUserStore.loginUser) {
    fetchUserApps()
  }
  fetchFeaturedApps()
})
</script>

<template>
  <div class="home-container">
    <div class="home-content">
      <!-- 网站标题 -->
      <div class="site-title">
        <h1>应用生成</h1>
        <p class="subtitle">和ちいかわ一起，把灵感生成成网页～</p>
      </div>
      
      <!-- 提示词输入框 -->
      <div class="prompt-section">
        <div class="prompt-input-wrapper">
          <Input.TextArea
            v-model:value="promptText"
            :placeholder="'比如：做个软糯温馨的个人主页，想要粉粉的配色和圆角卡片～'"
            :rows="5"
            :maxlength="5000"
            show-count
            class="prompt-input"
            @keyup.enter.ctrl="handleCreateApp"
          />
          <div class="prompt-actions">
            <Button type="primary" @click="handleCreateApp" :loading="isCreating">
              {{ isCreating ? '创作中…' : '开始创作' }}
            </Button>
          </div>
        </div>
        
        <!-- 快速提示词 -->
        <div class="quick-prompts">
          <div 
            v-for="(prompt, index) in ['小动物周边小店', '软糯温馨博客', '元气打卡日历', '冒泡留言板']"
            :key="index"
            class="quick-prompt-item"
            @click="promptText = prompt"
          >
            {{ prompt }}
          </div>
        </div>
      </div>
      
      
    </div>
  </div>
</template>

<style scoped>
.home-container {
  min-height: auto;
  background: url('@/assets/images/background.png') center/cover no-repeat fixed;
  position: relative;
}
.home-container::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(255,255,255,0.88) 0%, rgba(255,255,255,0.94) 60%, rgba(255,255,255,1) 100%);
  backdrop-filter: blur(1px);
}
.home-content {
  position: relative;
  z-index: 1;
  padding: 24px 0;
}

.site-title {
  text-align: center;
  margin-bottom: 48px;
}

.site-title h1 {
  font-size: 48px;
  margin-bottom: 12px;
  color: #66b3ff;
  letter-spacing: 0.5px;
}

.subtitle {
  font-size: 18px;
  color: #a6a6a6;
  margin: 0;
}

.prompt-section {
  max-width: 800px;
  margin: 0 auto 48px;
}

.prompt-input-wrapper {
  background: #f7fbff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 6px 24px rgba(102, 179, 255, 0.18);
  border: 1px solid #d6e8ff;
}

.prompt-input {
  margin-bottom: 16px;
}

.prompt-actions {
  text-align: right;
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 16px;
  justify-content: center;
}

.quick-prompt-item {
  padding: 10px 18px;
  background: #eaf4ff;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  color: #2b7fff;
}

.quick-prompt-item:hover {
  background: #d6e8ff;
  color: #1f5fd6;
}

</style>


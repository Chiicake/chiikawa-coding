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
        router.push(`/app/chat/${appId}?view=1`)
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

// 点击应用卡片（默认进入对话页，带 view=1 防止自动发送）
const handleAppClick = (app: any) => {
  router.push(`/app/chat/${app.id}?view=1`)
}

// 处理查看对话按钮
const handleViewChat = (app: any) => {
  router.push(`/app/chat/${app.id}?view=1`)
}

// 处理查看作品按钮：打开部署地址 localhost/{deployKey}
const handleViewWork = (app: any) => {
  if (app.deployKey) {
    const url = `http://localhost/${app.deployKey}`
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
    <!-- 网站标题 -->
    <div class="site-title">
      <h1>一句话 <span class="logo">🐱</span> 呈所想</h1>
      <p class="subtitle">与 AI 对话轻松创建应用和网站</p>
    </div>
    
    <!-- 提示词输入框 -->
    <div class="prompt-section">
      <div class="prompt-input-wrapper">
        <Input.TextArea
          v-model:value="promptText"
          :placeholder="'使用 NoCode 创建一个高效的小工具，帮我计算......'"
          :rows="2"
          :max-length="500"
          show-count
          class="prompt-input"
          @keyup.enter.ctrl="handleCreateApp"
        />
        <div class="prompt-actions">
          <Button type="primary" @click="handleCreateApp" :loading="isCreating">
            {{ isCreating ? '创建中...' : '开始创建' }}
          </Button>
        </div>
      </div>
      
      <!-- 快速提示词 -->
      <div class="quick-prompts">
        <div 
          v-for="(prompt, index) in ['波普风电商页面', '企业网站', '电商运营后台', '暗黑话题社区']"
          :key="index"
          class="quick-prompt-item"
          @click="promptText = prompt"
        >
          {{ prompt }}
        </div>
      </div>
    </div>
    
    <!-- 搜索框 -->
    <div class="search-section">
      <Input.Search
        v-model:value="appNameSearch"
        placeholder="搜索应用"
        enter-button="搜索"
        @search="handleSearch"
        style="max-width: 500px; margin: 0 auto 24px"
      />
    </div>
    
    <!-- 我的应用 -->
    <div v-if="loginUserStore.loginUser" class="apps-section">
      <Typography.Title :level="3" class="section-title">我的作品</Typography.Title>
      <div v-if="userAppsLoading && userApps.length === 0" class="loading-container">
        <Spin size="large" />
      </div>
      <div v-else-if="userApps.length === 0" class="empty-container">
        <Empty description="暂无应用，快去创建吧！" />
      </div>
      <div v-else class="apps-grid">
        <Row :gutter="[16, 16]">
          <Col 
            v-for="app in userApps" 
            :key="app.id" 
            xs={24} 
            sm={12} 
            md={8} 
            class="app-col"
          >
            <Card 
              :cover="{ 
                src: app.cover || getDefaultCover(),
                alt: app.appName 
              }" 
              class="app-card"
              @click="handleAppClick(app)"
            >
              <div class="card-overlay" @click.stop>
                <Button class="overlay-btn overlay-dark" v-if="app.deployKey" @click.stop="handleViewWork(app)">查看作品</Button>
                <Button class="overlay-btn overlay-light" @click.stop="handleViewChat(app)">查看对话</Button>
              </div>
              <Card.Meta 
                :title="app.appName || '未命名应用'"
                :description="`创建于 ${new Date(app.createTime || Date.now()).toLocaleDateString()}`"
              />
            </Card>
          </Col>
        </Row>
        
        <!-- 加载更多 -->
        <div v-if="hasMoreUserApps" class="load-more">
          <Button 
            @click="loadMoreUserApps"
            :loading="userAppsLoading"
            type="link"
          >
            加载更多
          </Button>
        </div>
      </div>
    </div>
    
    <!-- 精选应用 -->
    <div class="apps-section">
      <Typography.Title :level="3" class="section-title">精选案例</Typography.Title>
      <div v-if="featuredAppsLoading && featuredApps.length === 0" class="loading-container">
        <Spin size="large" />
      </div>
      <div v-else-if="featuredApps.length === 0" class="empty-container">
        <Empty description="暂无精选应用" />
      </div>
      <div v-else class="apps-grid">
        <Row :gutter="[16, 16]">
          <Col 
            v-for="app in featuredApps" 
            :key="app.id" 
            xs={24} 
            sm={12} 
            md={8} 
            class="app-col"
          >
            <Card 
              :cover="{ 
                src: app.cover || getDefaultCover(),
                alt: app.appName 
              }" 
              class="app-card"
              @click="handleAppClick(app)"
            >
              <div class="card-overlay" @click.stop>
                <Button class="overlay-btn overlay-dark" v-if="app.deployKey" @click.stop="handleViewWork(app)">查看作品</Button>
                <Button class="overlay-btn overlay-light" @click.stop="handleViewChat(app)">查看对话</Button>
              </div>
              <Card.Meta 
                :title="app.appName || '未命名应用'"
                :description="`创建于 ${new Date(app.createTime || Date.now()).toLocaleDateString()}`"
              />
            </Card>
          </Col>
        </Row>
        
        <!-- 加载更多 -->
        <div v-if="hasMoreFeaturedApps" class="load-more">
          <Button 
            @click="loadMoreFeaturedApps"
            :loading="featuredAppsLoading"
            type="link"
          >
            加载更多
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  padding: 24px 0;
}

.site-title {
  text-align: center;
  margin-bottom: 48px;
}

.site-title h1 {
  font-size: 48px;
  margin-bottom: 16px;
  color: #1890ff;
}

.logo {
  font-size: 36px;
}

.subtitle {
  font-size: 18px;
  color: #666;
  margin: 0;
}

.prompt-section {
  max-width: 800px;
  margin: 0 auto 48px;
}

.prompt-input-wrapper {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
  padding: 8px 16px;
  background: #f0f0f0;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.quick-prompt-item:hover {
  background: #e6f7ff;
  color: #1890ff;
}

.search-section {
  text-align: center;
}

.apps-section {
  margin-bottom: 48px;
}

.section-title {
  margin-bottom: 24px;
  text-align: center;
  color: #333;
}

.apps-grid {
  margin-top: 24px;
}

.app-col {
  margin-bottom: 16px;
}

.app-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.app-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-4px);
}

.card-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: none;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  z-index: 2;
}

.app-card:hover .card-overlay {
  display: flex;
}

.overlay-btn {
  border-radius: 24px;
  padding: 10px 18px;
}

.overlay-dark {
  background: #000;
  color: #fff;
  border: none;
}

.overlay-light {
  background: #fff;
  color: #000;
}

.loading-container,
.empty-container {
  padding: 64px 0;
  text-align: center;
}

.load-more {
  text-align: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .site-title h1 {
    font-size: 36px;
  }
  
  .subtitle {
    font-size: 16px;
  }
  
  .prompt-input-wrapper {
    padding: 16px;
  }
}
</style>
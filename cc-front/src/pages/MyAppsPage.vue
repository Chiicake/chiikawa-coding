<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { listUserAppVoByPage, listFeaturedAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { Button, Input, Card, Row, Col, Typography, Empty, Spin } from 'ant-design-vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 列表与状态
const userApps = ref<any[]>([])
const featuredApps = ref<any[]>([])
const userAppsLoading = ref(false)
const featuredAppsLoading = ref(false)
const userPageNum = ref(1)
const featuredPageNum = ref(1)
const hasMoreUserApps = ref(true)
const hasMoreFeaturedApps = ref(true)
const appNameSearch = ref('')

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
      const total = response.data.data?.total || 0
      const currentCount = userApps.value.length
      hasMoreUserApps.value = currentCount < total
    } else {
      message.error(response.data.message || '获取我的应用失败')
    }
  } catch (e) {
    message.error('获取我的应用失败')
  } finally {
    userAppsLoading.value = false
  }
}

// 获取精选/大家灵感列表
const fetchFeaturedApps = async (pageNum: number = 1, isLoadMore: boolean = false) => {
  if (featuredAppsLoading.value || (!isLoadMore && !hasMoreFeaturedApps.value)) return
  featuredAppsLoading.value = true
  try {
    const queryParams = {
      pageNum,
      pageSize: 9,
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
      const total = response.data.data?.total || 0
      const currentCount = featuredApps.value.length
      hasMoreFeaturedApps.value = currentCount < total
    } else {
      message.error(response.data.message || '获取大家的灵感失败')
    }
  } catch (e) {
    message.error('获取大家的灵感失败')
  } finally {
    featuredAppsLoading.value = false
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

// 加载更多
const loadMoreUserApps = () => {
  if (!hasMoreUserApps.value) return
  userPageNum.value += 1
  fetchUserApps(userPageNum.value, true)
}

const loadMoreFeaturedApps = () => {
  if (!hasMoreFeaturedApps.value) return
  featuredPageNum.value += 1
  fetchFeaturedApps(featuredPageNum.value, true)
}

// 卡片交互
const handleAppClick = (app: any) => {
  router.push(`/app/chat/${app.id}`)
}

// 查看对话：携带 noAuto=1，避免自动初始发送
const handleViewChat = (app: any) => {
  router.push(`/app/chat/${app.id}?noAuto=1`)
}

// 查看作品：打开部署地址 localhost/{deployKey}
const handleViewWork = (app: any) => {
  if (app.deployKey) {
    const url = `http://localhost:81/${app.deployKey}`
    window.open(url, '_blank')
  } else {
    message.warning('该应用尚未部署')
  }
}

// 默认封面
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
  <div class="apps-container">
    <!-- 搜索框 -->
    <div class="search-section">
      <Input.Search
        v-model:value="appNameSearch"
        placeholder="搜一搜大家的作品～"
        enter-button="搜一下"
        @search="handleSearch"
        style="max-width: 500px; margin: 0 auto 24px"
      />
    </div>

    <!-- 我的应用/我的小作品 -->
    <div v-if="loginUserStore.loginUser" class="apps-section">
      <Typography.Title :level="3" class="section-title">我的作品</Typography.Title>
      <div v-if="userAppsLoading && userApps.length === 0" class="loading-container">
        <Spin size="large" />
      </div>
      <div v-else-if="userApps.length === 0" class="empty-container">
        <Empty description="还没有作品呢～ 快来试试吧！" />
      </div>
      <div v-else class="apps-grid">
        <Row :gutter="[16, 16]" justify="space-between" align="top">
          <Col
            v-for="app in userApps"
            :key="app.id"
            :xs="24"
            :sm="12"
            :md="8"
            class="app-col"
          >
            <Card
              class="app-card"
              @click="handleAppClick(app)"
            >
              <template #cover>
                <div class="cover-wrapper">
                  <img :src="app.cover || getDefaultCover()" :alt="app.appName" class="card-cover-img" loading="lazy" />
                </div>
              </template>
              <div class="card-overlay" @click.stop>
                <Button class="overlay-btn overlay-dark" v-if="app.deployKey" @click.stop="handleViewWork(app)">去看看</Button>
                <Button class="overlay-btn overlay-light" @click.stop="handleViewChat(app)">聊一聊</Button>
              </div>
              <div class="card-meta">
                <Card.Meta
                  :title="app.appName || '未命名应用'"
                  :description="`创建于 ${new Date(app.createTime || Date.now()).toLocaleDateString()}`"
                />
              </div>
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
            再多看看
          </Button>
        </div>
      </div>
    </div>

    <!-- 大家的灵感 -->
    <div class="apps-section">
      <Typography.Title :level="3" class="section-title">精选作品</Typography.Title>
      <div v-if="featuredAppsLoading && featuredApps.length === 0" class="loading-container">
        <Spin size="large" />
      </div>
      <div v-else-if="featuredApps.length === 0" class="empty-container">
        <Empty description="暂时还没有精选～" />
      </div>
      <div v-else class="apps-grid">
        <Row :gutter="[16, 16]" justify="space-between" align="top">
          <Col
            v-for="app in featuredApps"
            :key="app.id"
            :xs="24"
            :sm="12"
            :md="8"
            class="app-col"
          >
            <Card
              class="app-card"
              @click="handleAppClick(app)"
            >
              <template #cover>
                <div class="cover-wrapper">
                  <img :src="app.cover || getDefaultCover()" :alt="app.appName" class="card-cover-img" loading="lazy" />
                </div>
              </template>
              <div class="card-overlay" @click.stop>
                <Button class="overlay-btn overlay-dark" v-if="app.deployKey" @click.stop="handleViewWork(app)">去看看</Button>
                <Button class="overlay-btn overlay-light" @click.stop="handleViewChat(app)">聊一聊</Button>
              </div>
              <div class="card-meta">
                <Card.Meta
                  :title="app.appName || '未命名应用'"
                  :description="`创建于 ${new Date(app.createTime || Date.now()).toLocaleDateString()}`"
                />
              </div>
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
            再多看看
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.apps-container {
  max-width: 1080px;
  margin: 0 auto;
  padding: 24px 16px;
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
  color: #3ea0ff;
}

.apps-grid {
  margin-top: 24px;
}

.app-col {
  margin-bottom: 16px;
  display: flex;
}

.app-card {
  cursor: pointer;
  transition: all 0.25s ease;
  height: 100%;
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.app-card:hover {
  box-shadow: 0 8px 24px rgba(102, 179, 255, 0.25);
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
  border-radius: 999px;
  padding: 10px 18px;
}

.overlay-dark {
  background: #66b3ff;
  color: #fff;
  border: none;
}

.overlay-light {
  background: #fff;
  color: #66b3ff;
  border: 1px solid #d6e8ff;
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

.cover-wrapper {
  width: 100%;
  height: 120px;
  background: #f5f7fa;
  border-bottom: 1px solid #eef2f7;
}

.card-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.card-meta {
  min-height: 64px;
  margin-top: 8px;
}

.app-card :deep(.ant-card-body) {
  padding: 10px;
}

.app-card {
  min-height: 220px;
  height: 240px;
}
</style>

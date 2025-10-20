<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { listUserAppVoByPage, deleteApp, updateAppForAdmin } from '@/api/appController'
import { getUserVoById } from '@/api/userController'
// 移除不存在的类型导入
// import { useLoginUserStore } from '@/stores/loginUser'
import { Table, Button, Input, Select, Upload, Typography, Space, Popconfirm } from 'ant-design-vue'


const router = useRouter()
// const loginUserStore = useLoginUserStore()
const apps = ref<any[]>([])
const loading = ref(false)
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0
})
const searchParams = ref<any>({
  pageNum: 1,
  pageSize: 20,
  appName: '',
  orderBy: 'createTime',
  sortBy: 'desc'
})

// 获取应用列表
const fetchApps = async () => {
  loading.value = true
  try {
    const response = await listUserAppVoByPage(searchParams.value)
    if (response.data.code === 0) {
      const records = response.data.data?.records || []
      // 并行获取创建用户名称
      const withCreators = await Promise.all(
        records.map(async (app: any) => {
          let creatorName = '-' 
          try {
            if (app.userId) {
              const uRes = await getUserVoById({ id: app.userId })
              if (uRes?.data?.code === 0 && uRes?.data?.data) {
                creatorName = uRes.data.data.userName || uRes.data.data.userAccount || '-'
              }
            }
          } catch (e) {
            // 忽略单个错误，使用默认'-'
          }
          return { ...app, creatorName }
        })
      )
      apps.value = withCreators
      pagination.value.total = response.data.data?.totalRow || 0
    }
  } catch (error) {
    message.error('获取应用列表失败')
  } finally {
    loading.value = false
  }
}

// 删除应用
const handleDelete = async (appId: string) => {
  try {
    const response = await deleteApp({ id: appId })
    if (response.data.code === 0) {
      message.success('删除成功')
      // 重新获取列表
      fetchApps()
    } else {
      message.error(response.data.message || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
  }
}

// 设为精选
const handleFeatured = async (appId: string) => {
  try {
    const response = await updateAppForAdmin({
      id: appId,
      priority: 99 // 优先级设为99表示精选
    } as any)
    if (response.data.code === 0) {
      message.success('设置成功')
      // 更新本地数据
      const app = apps.value.find((a) => a.id === appId)
      if (app) {
        app.priority = 99
      }
    } else {
      message.error(response.data.message || '设置失败')
    }
  } catch (error) {
    message.error('设置失败')
  }
}

// 编辑应用
const handleEdit = (appId: string) => {
  router.push(`/app/edit/${appId}`)
}

// 查看应用（使用 view=1 避免进入后自动发送消息）
const handleView = (appId: string) => {
  router.push(`/app/chat/${appId}?view=1`)
}

// 搜索
const handleSearch = () => {
  pagination.value.current = 1
  searchParams.value.pageNum = 1
  fetchApps()
}

// 重置搜索
const handleReset = () => {
  searchParams.value.appName = ''
  pagination.value.current = 1
  searchParams.value.pageNum = 1
  fetchApps()
}

onMounted(() => {
  fetchApps()
})
</script>

<template>
  <div class="app-manage-container">
    <div class="page-header">
      <Typography.Title :level="4">应用管理</Typography.Title>
      <div class="action-bar">
        <Space>
          <Input v-model:value="searchParams.appName" placeholder="搜索应用名称" allow-clear />
          <Button type="primary" @click="handleSearch">搜索</Button>
          <Button @click="handleReset">重置</Button>
        </Space>
      </div>
    </div>

    <Table
      :dataSource="apps"
      :loading="loading"
      :pagination="false"
      rowKey="id"
    >
      <template #headerCell="{ column }">
        <template v-if="column.key === 'index'">
          序号
        </template>
        <template v-else>
          {{ column.title }}
        </template>
      </template>
      
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.key === 'index'">
          {{ index + 1 }}
        </template>
        <template v-else-if="column.dataIndex === 'appName'">
          {{ record.appName || '未命名应用' }}
        </template>
        <template v-else-if="column.dataIndex === 'creatorName'">
          {{ record.creatorName || '-' }}
        </template>
        <template v-else-if="column.dataIndex === 'priority'">
          {{ record.priority ?? 0 }}
        </template>
        <template v-else-if="column.dataIndex === 'cover'">
          <img :src="record.cover" alt="cover" style="width: 80px; height: 40px; object-fit: cover;" v-if="record.cover" />
          <span v-else>-</span>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ new Date(record.createTime || Date.now()).toLocaleString() }}
        </template>
        <template v-else-if="column.dataIndex === 'action'">
          <Space>
            <Button type="primary" @click="handleEdit(record.id)">编辑</Button>
            <Popconfirm title="确定删除该应用？" ok-text="删除" cancel-text="取消" @confirm="handleDelete(record.id)">
              <Button danger>删除</Button>
            </Popconfirm>
            <Button @click="handleFeatured(record.id)">设为精选</Button>
          </Space>
        </template>
      </template>

      <template #footer>
        <div class="footer-bar">
          <Button @click="() => { searchParams.pageNum++; fetchApps() }" :disabled="loading">加载更多</Button>
        </div>
      </template>
    </Table>
  </div>
</template>

<style scoped>
.app-manage-container {
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.action-bar {
  display: flex;
  align-items: center;
}

.footer-bar {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}
</style>
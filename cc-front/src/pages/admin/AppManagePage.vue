<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { listUserAppVoByPage, deleteApp, updateApp } from '@/api/appController'
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
      apps.value = response.data.data?.records || []
      // 使用可选链安全访问total属性
      pagination.value.total = (response.data.data as any)?.total || 0
    } else {
      message.error(response.data.message || '获取应用列表失败')
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
    // 正确构造AppUpdateRequest对象
    const appUpdateRequest = {
      priority: 99 // 优先级设为99表示精选
    } as any
    
    const response = await updateApp({
      id: appId,
      ...appUpdateRequest
    })
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

// 查看应用
const handleView = (appId: string) => {
  router.push(`/app/chat/${appId}`)
}

// 表格列配置
const columns: any = [
  {
    title: '应用名称',
    dataIndex: 'appName',
    key: 'appName',
    ellipsis: true,
    slots: {
      customRender: 'appName'
    }
  },
  {
    title: '创建用户',
    dataIndex: ['user', 'username'],
    key: 'username',
    ellipsis: true
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    render: (text: string) => {
      return text ? new Date(text).toLocaleString() : '-'
    }
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    key: 'priority',
    render: (text: number) => {
      return text === 99 ? '精选' : (text || '-')
    }
  },
  {
    title: '部署状态',
    dataIndex: 'deployUrl',
    key: 'deployUrl',
    render: (text: string) => {
      return text ? '已部署' : '未部署'
    }
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    slots: {
      customRender: 'action'
    }
  }
]

// 搜索
const handleSearch = () => {
  pagination.value.current = 1
  searchParams.value.pageNum = 1
  fetchApps()
}

// 重置搜索
const handleReset = () => {
  searchParams.value = {
    pageNum: 1,
    pageSize: 20,
    appName: '',
    orderBy: 'createTime',
    sortBy: 'desc'
  }
  pagination.value.current = 1
  fetchApps()
}

// 分页变化
const handlePageChange = (page: number, pageSize: number) => {
  pagination.value.current = page
  pagination.value.pageSize = pageSize
  searchParams.value.pageNum = page
  searchParams.value.pageSize = pageSize
  fetchApps()
}

// 排序变化
const handleTableChange = (pagination: any, filters: any, sorter: any) => {
  if (sorter.field && sorter.order) {
    searchParams.value.orderBy = sorter.field
    searchParams.value.sortBy = sorter.order === 'ascend' ? 'asc' : 'desc'
    fetchApps()
  }
}

// 检查管理员权限
// const checkAdminPermission = async () => {
//   await loginUserStore.fetchLoginUser()
//   // 由于类型定义问题，这里使用可选链操作符安全访问
//   // 改用userRole属性进行权限检查
//   if (loginUserStore.loginUser?.userRole !== 'ADMIN') {
//     message.error('没有权限访问此页面')
//     router.push('/noAuth')
//     return false
//   }
//   return true
// }

onMounted(() => {
  fetchApps()
})
</script>

<template>
  <div class="app-manage-container">
    <div class="page-header">
      <Typography.Title :level="4">应用管理</Typography.Title>
    </div>
    <!-- 搜索栏 -->
    <div class="search-bar">
      <Space wrap>
        <Input
          v-model:value="searchParams.appName"
          placeholder="应用名称"
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
        <Button type="primary" @click="handleSearch">
          搜索
        </Button>
        <Button @click="handleReset">
          重置
        </Button>
      </Space>
    </div>
    
    <!-- 数据表格 -->
    <Table
      :columns="columns"
      :data-source="apps"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
      row-key="id"
      class="apps-table"
    >
      <!-- 使用columns的scopedSlots定义自定义渲染，而不是直接在Table组件上使用插槽 -->
        <template v-slot:bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'appName'">
            <Typography.Text 
              ellipsis 
              :tooltip="record.appName"
              className="app-name"
              @click="handleView(record.id)"
              style="max-width: 300px; display: inline-block;"
            >
              {{ record.appName }}
            </Typography.Text>
          </template>
          <template v-else-if="column.dataIndex === 'deployUrl'">
            <span>{{ record.deployUrl ? '已部署' : '未部署' }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <Space size="middle">
              <Button type="link" @click="handleEdit(record.id)">
                编辑
              </Button>
              <Popconfirm
                title="确定要删除该应用吗？"
                description="删除后无法恢复"
                @confirm="handleDelete(record.id)"
                ok-text="确定"
                cancel-text="取消"
              >
                <Button type="link" danger>
                  删除
                </Button>
              </Popconfirm>
              <template v-if="record.priority !== 99">
                <Button type="link" @click="handleFeatured(record.id)">
                  精选
                </Button>
              </template>
            </Space>
          </template>
        </template>
    </Table>
  </div>
</template>

<style scoped>
.app-manage-container {
  padding: 24px;
  background: #fff;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 16px;
}

.search-bar {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
}

.apps-table {
  background: #fff;
}

.app-name {
  cursor: pointer;
  color: #1890ff;
}

.app-name:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .app-manage-container {
    padding: 16px;
  }
  
  .search-bar {
    padding: 12px;
  }
  
  .search-bar :deep(.ant-space) {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .search-bar :deep(.ant-input) {
    width: 100% !important;
  }
}
</style>
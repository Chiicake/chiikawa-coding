<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { Table, Button, Input, Select, Typography, Space, Popconfirm } from 'ant-design-vue'
import { listAllChatHistoryByPageForAdmin, remove1 } from '@/api/chatHistoryController'

const router = useRouter()
const histories = ref<any[]>([])
const loading = ref(false)
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
})

const searchParams = ref<any>({
  pageNum: 1,
  pageSize: 20,
  appId: undefined,
  userId: undefined,
  messageType: undefined,
  message: '',
  sortField: 'createTime',
  sortOrder: 'descend',
})

const columns = [
  { title: '序号', key: 'index' },
  { title: '应用ID', dataIndex: 'appId', key: 'appId' },
  { title: '用户ID', dataIndex: 'userId', key: 'userId' },
  { title: '类型', dataIndex: 'messageType', key: 'messageType' },
  { title: '消息内容', dataIndex: 'message', key: 'message' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', dataIndex: 'action', key: 'action' },
]

const fetchHistories = async () => {
  loading.value = true
  try {
    const res = await listAllChatHistoryByPageForAdmin(searchParams.value)
    if (res?.data?.code === 0) {
      histories.value = (res.data.data?.records || []).map((r: any, idx: number) => ({
        ...r,
        index: (searchParams.value.pageNum - 1) * searchParams.value.pageSize + idx + 1,
      }))
      pagination.value.total = res.data.data?.totalRow || 0
    }
  } catch (e) {
    message.error('获取对话历史失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  searchParams.value.pageNum = 1
  pagination.value.current = 1
  fetchHistories()
}

const handleReset = () => {
  searchParams.value.appId = undefined
  searchParams.value.userId = undefined
  searchParams.value.messageType = undefined
  searchParams.value.message = ''
  searchParams.value.pageNum = 1
  pagination.value.current = 1
  fetchHistories()
}

const handleViewApp = (appId: number) => {
  router.push(`/app/chat/${appId}`)
}

const handleDelete = async (id: number) => {
  try {
    const res = await remove1({ id })
    if (res === true || res?.data === true) {
      message.success('删除成功')
      fetchHistories()
    } else {
      message.error('删除失败')
    }
  } catch (e) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchHistories()
})
</script>

<template>
  <div class="chat-manage-container">
    <div class="page-header">
      <Typography.Title :level="4">对话管理</Typography.Title>
      <div class="action-bar">
        <Space>
          <Input v-model:value="searchParams.appId" placeholder="应用ID" style="width: 140px" />
          <Input v-model:value="searchParams.userId" placeholder="用户ID" style="width: 140px" />
          <Select v-model:value="searchParams.messageType" placeholder="消息类型" allow-clear style="width: 140px">
            <Select.Option value="user">用户</Select.Option>
            <Select.Option value="ai">AI</Select.Option>
          </Select>
          <Input v-model:value="searchParams.message" placeholder="消息内容(模糊)" allow-clear style="width: 200px" />
          <Button type="primary" @click="handleSearch">搜索</Button>
          <Button @click="handleReset">重置</Button>
        </Space>
      </div>
    </div>

    <Table
      :dataSource="histories"
      :loading="loading"
      :pagination="{ current: pagination.current, pageSize: pagination.pageSize, total: pagination.total, showSizeChanger: false }"
      rowKey="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'index'">
          {{ record.index }}
        </template>
        <template v-else-if="column.key === 'message'">
          <span style="display:inline-block; max-width: 420px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
            {{ record.message }}
          </span>
        </template>
        <template v-else-if="column.key === 'action'">
          <Space>
            <Button type="link" @click="handleViewApp(record.appId)">查看应用</Button>
            <Popconfirm title="确认删除该消息？" @confirm="handleDelete(record.id)">
              <Button danger type="link">删除</Button>
            </Popconfirm>
          </Space>
        </template>
        <template v-else>
          {{ record[column.dataIndex] }}
        </template>
      </template>
    </Table>
  </div>
</template>

<style scoped>
.chat-manage-container {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.action-bar {
  display: flex;
  align-items: center;
}
</style>

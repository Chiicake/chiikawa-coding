<script setup lang="ts">
import BasicLayout from './layouts/BasicLayout.vue'

import { healthCheck } from '@/api/healthController.ts'

healthCheck().then((res) => {
  console.log(res)
})

// import { useLoginUserStore } from '@/stores/loginUser.ts'

// const loginUserStore = useLoginUserStore()
// loginUserStore.fetchLoginUser()

</script>

<template>
  <BasicLayout />
</template>

<style scoped>
/* 全局样式已在BasicLayout和相关组件中定义 */
</style>

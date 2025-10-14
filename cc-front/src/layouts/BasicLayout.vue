<template>
  <a-layout class="basic-layout">
    <global-header />
    
    <a-layout-content class="content-wrapper">
      <div class="content">
        <router-view />
      </div>
    </a-layout-content>
    
    <global-footer />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '../components/GlobalHeader.vue'
import GlobalFooter from '../components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-image: url('@/assets/images/background.png');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  position: relative;
}

.basic-layout::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8); /* 设置白色遮罩层，透明度为0.2，使背景图片变透明 */
  z-index: 0;
}

.basic-layout > * {
  position: relative;
  z-index: 1;
}

.content-wrapper {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.content {
  max-width: 1200px;
  margin: 0 auto;
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  min-height: calc(100vh - 200px);
}

@media (max-width: 768px) {
  .content-wrapper {
    padding: 12px;
  }
  
  .content {
    padding: 16px;
    min-height: calc(100vh - 180px);
  }
}
</style>
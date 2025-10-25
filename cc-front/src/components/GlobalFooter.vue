<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <div class="copyright">
        <span>© {{ currentYear }} Chiicake</span>
        <span class="divider">|</span>
        <a :href="githubUrl" target="_blank" rel="noopener noreferrer">
          开源代码
        </a>
      </div>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const githubUrl = ref('https://github.com/Chiicake/chiikawa-coding')
const currentYear = ref(new Date().getFullYear())

onMounted(() => {
  // 确保年份正确
  currentYear.value = new Date().getFullYear()
})
</script>

<style scoped>
.footer {
  background: #f0f2f5;
  padding: 24px 0;
  text-align: center;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.copyright {
  font-size: 14px;
  color: #666;
}

.divider {
  margin: 0 8px;
  color: #d9d9d9;
}

.copyright a {
  color: #1890ff;
  text-decoration: none;
}

.copyright a:hover {
  color: #40a9ff;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-content {
    padding: 0 12px;
  }
  
  .copyright {
    font-size: 12px;
  }
  
  .divider {
    display: none;
  }
  
  .copyright {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
}
</style>
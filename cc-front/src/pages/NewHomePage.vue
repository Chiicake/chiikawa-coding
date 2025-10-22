<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Button } from 'ant-design-vue'

const router = useRouter()

const goGenerate = () => router.push('/generate')
</script>

<template>
  <div class="new-home-container">
    <div class="hero">
      <div class="hero-content">
        <div class="brand">Chiikawa Coding</div>
        <h1 class="title">可爱的编程，让想法变网页</h1>
        <p class="desc">和ちいかわ一起，轻松把灵感生成成网站与应用。</p>
        <div class="actions">
          <Button type="primary" size="large" @click="goGenerate">开始生成</Button>
          <Button size="large" @click="goGenerate" class="secondary">随便逛逛</Button>
        </div>
      </div>
    </div>
    <div class="features">
      <div class="feature-card">
        <div class="icon">✨</div>
        <div class="name">一键生成</div>
        <div class="tip">输入提示词，立刻获得初版网站</div>
      </div>
      <div class="feature-card">
        <div class="icon">🧸</div>
        <div class="name">软糯风格</div>
        <div class="tip">圆角、柔色、可爱主题风随心搭配</div>
      </div>
      <div class="feature-card">
        <div class="icon">🚀</div>
        <div class="name">本地预览</div>
        <div class="tip">生成即可预览，快速迭代你的想法</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.new-home-container {
  display: flex;
  flex-direction: column;
}

.hero {
  position: relative;
  min-height: 56vh;
  border-radius: 16px;
  overflow: hidden;
  background: url('/src/assets/images/background.png') center/cover no-repeat;
  box-shadow: 0 6px 24px rgba(0,0,0,0.08);
}

.hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(255,255,255,0.85), rgba(255,255,255,0.55));
}

.hero-content {
  position: relative;
  z-index: 1;
  padding: 64px 32px;
  max-width: 960px;
  margin: 0 auto;
  text-align: center;
}

.brand {
  display: inline-block;
  padding: 6px 12px;
  background: rgba(102, 179, 255, 0.15);
  color: #66b3ff;
  border: 1px solid #66b3ff55;
  border-radius: 999px;
  font-size: 14px;
  margin-bottom: 12px;
}

.title {
  font-size: 40px;
  color: #333;
  margin: 8px 0 12px;
}

.desc {
  font-size: 16px;
  color: #666;
}

.actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: center;
}

.secondary {
  background: #fff;
}

.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 24px;
}

.feature-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  border: 1px solid #eee;
}

.icon {
  font-size: 28px;
}

.name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-top: 6px;
}

.tip {
  font-size: 14px;
  color: #666;
}

@media (max-width: 768px) {
  .title { font-size: 28px; }
  .features { grid-template-columns: 1fr; }
  .hero { min-height: 52vh; }
}
</style>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Button } from 'ant-design-vue'
import iconImg from '@/assets/images/icon.png'

const router = useRouter()

const goGenerate = () => router.push('/generate')
const goMyApps = () => router.push('/my-apps')
const goAbout = () => router.push('/about')
</script>

<template>
  <div class="new-home-container">
    <!-- 顶部 Hero -->
    <div class="hero">
      <div class="hero-content">
        <div class="brand">Chiicake</div>
        <h1 class="title">Chiicake</h1>
        <p class="desc">欢迎！这是我的个人网站，用于实验一些新学到的技术！</p>
        <div class="actions">
          <!-- <Button type="primary" size="large" @click="goMyApps">我的作品</Button> -->
          <Button size="large" @click="goGenerate" class="secondary">应用生成</Button>
        </div>
      </div>
    </div>

    <!-- 关于我 -->
    <section class="about">
      <div class="about-card">
        <img :src="iconImg" alt="avatar" class="avatar" />
        <div class="about-text">
          <h2>关于我</h2>
          <p>
            你好，我是 Chiicake。
          </p>
          <div class="about-actions">
            <Button @click="goAbout">更多介绍</Button>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<style scoped>
.new-home-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* Hero */
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

/* 关于我 */
.about {
  display: flex;
  justify-content: center;
}
.about-card {
  display: flex;
  gap: 24px;
  align-items: center;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 16px;
  padding: 24px;
  max-width: 960px;
  width: 92%;
  box-shadow: 0 6px 16px rgba(0,0,0,0.06);
}
.avatar {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  border: 6px solid #eaf4ff;
  background: #fff;
}
.about-text h2 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #333;
}
.about-text p {
  margin: 0;
  color: #666;
  line-height: 1.7;
}
.about-actions { margin-top: 12px; }

/* 作品预览 */
.works { max-width: 1080px; margin: 0 auto; padding: 0 16px; }
.section-title { font-size: 22px; color: #333; margin-bottom: 12px; }
.work-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.work-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all .2s ease;
}
.work-card:hover { box-shadow: 0 8px 18px rgba(0,0,0,0.08); transform: translateY(-2px); }
.work-thumb { font-size: 28px; }
.work-name { font-size: 18px; font-weight: 600; color: #333; margin-top: 6px; }
.work-tip { font-size: 14px; color: #666; }
.works-actions { margin-top: 8px; text-align: right; }

/* CTA */
.cta { display: flex; justify-content: center; }
.cta-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 92%;
  max-width: 960px;
  padding: 20px 24px;
  background: linear-gradient(90deg, #f7fbff, #ffffff);
  border: 1px solid #eaf4ff;
  border-radius: 16px;
}
.cta-text h3 { margin: 0 0 6px; font-size: 18px; color: #2b7fff; }
.cta-text p { margin: 0; color: #666; }

/* 页脚 */
.footer { background: transparent; }
.footer-content { text-align: center; padding: 12px 0 24px; }
.footer-brand { color: #66b3ff; font-weight: 600; }
.footer-links { display: flex; justify-content: center; gap: 16px; margin: 8px 0; }
.footer-links a { color: #666; text-decoration: none; }
.footer-links a:hover { color: #333; }
.footer-copy { color: #aaa; font-size: 13px; }

/* 响应式 */
@media (max-width: 768px) {
  .title { font-size: 28px; }
  .hero { min-height: 52vh; }
  .work-grid { grid-template-columns: 1fr; }
  .about-card { flex-direction: column; text-align: center; }
}
</style>


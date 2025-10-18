import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// 引入ant-design-vue组件
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

// 引入全局样式
import './assets/global.css'

// 启用基于 meta.access 的全局路由权限守卫
import './access/index.ts'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)

app.mount('#app')
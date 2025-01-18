import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// 禁止控制台输出
console.log = function () {}
console.error = function () {}
console.warn = function () {}

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')

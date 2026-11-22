import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

import Components from 'unplugin-vue-components/vite'
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
import pkg from './package.json'

// https://vite.dev/config/
export default defineConfig({
  define: {
    // 版本号单一来源为后端 pom.xml：随 Maven 构建注入 VITE_APP_VERSION；
    // 独立 pnpm dev/build 时回退读取 package.json
    __APP_VERSION__: JSON.stringify(process.env.VITE_APP_VERSION || pkg.version),
  },
  server: {
    proxy: {
      // 开发模式：pnpm dev 热更新时 API 与 WebSocket 自动转发到本机 MagicShare 服务
      '/api': { target: 'http://127.0.0.1:80', changeOrigin: true },
      '/ws': { target: 'ws://127.0.0.1:80', ws: true },
    },
  },
  plugins: [
    vue(),
    vueDevTools(),
    Components({
      resolvers: [
        AntDesignVueResolver({
          importStyle: false, // css in js
        }),
      ],
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
})

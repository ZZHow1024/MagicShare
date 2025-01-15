<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const selectedKeys = ref([route.path])
const onChangeMenu = (page) => {
  if (selectedKeys.value === page) return

  router.push(page)
  selectedKeys.value = [page]
}
</script>

<template>
  <div class="layout-container">
    <a-layout class="layout">
      <a-layout-header>
        <span class="app-title">MagicShare</span>
        <div class="logo" @click="onChangeMenu('/home')">
          <img
            class="logo-img"
            src="@/assets/MagicShare.png"
            height="488"
            width="522"
            alt="MagicShareLogo"
          />
        </div>
        <a-menu
          v-model:selectedKeys="selectedKeys"
          theme="dark"
          mode="horizontal"
          :style="{ lineHeight: '64px' }"
        >
          <a-menu-item key="/home" @click="onChangeMenu('/home')">文件列表</a-menu-item>
          <a-menu-item key="/about" @click="onChangeMenu('/about')">关于</a-menu-item>
        </a-menu>
      </a-layout-header>
      <a-layout-content>
        <router-view></router-view>
      </a-layout-content>

      <a-layout-footer style="text-align: center"> ZZHow </a-layout-footer>
    </a-layout>
  </div>
</template>

<style scoped lang="scss">
.layout {
  position: fixed;
  left: 0;
  top: 0;
  width: 100vw;
  height: 100vh;
}

.site-layout-content {
  min-height: 280px;
  padding: 24px;
  background: #fff;
}
.layout {
  .app-title {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    text-align: center;
    font-size: 2.5vw;
    color: white;
  }

  .logo {
    position: fixed;
    right: 3vw;
    height: 8vh;
    width: auto;
    cursor: pointer;
    .logo-img {
      width: 100%;
      height: 100%;
    }
  }
}

[data-theme='dark'] .site-layout-content {
  background: #141414;
}
</style>

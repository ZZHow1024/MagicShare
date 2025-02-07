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
        <div class="logo">
          <span class="app-title" @click="onChangeMenu('/home')">MagicShare</span>
          <span>
            <img
              class="logo-img"
              src="@/assets/MagicShare.png"
              height="488"
              width="522"
              alt="MagicShareLogo"
              @click="onChangeMenu('/home')"
            />
          </span>
        </div>
        <a-menu
          v-model:selectedKeys="selectedKeys"
          theme="dark"
          mode="horizontal"
          :style="{ lineHeight: '64px', marginLeft: '-5px' }"
        >
          <a-menu-item key="/home" @click="onChangeMenu('/home')">{{
            $t('home.title')
          }}</a-menu-item>
          <a-menu-item key="/about" @click="onChangeMenu('/about')">{{
            $t('about.title')
          }}</a-menu-item>
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
  .logo {
    position: fixed;
    right: 3vw;
    height: 8vh;
    width: auto;
    display: flex;

    .app-title {
      position: relative;
      top: 0;
      right: 3vw;
      width: 100%;
      text-align: center;
      font-size: 2.5vw;
      cursor: pointer;
      color: white;
    }

    .logo-img {
      width: auto;
      height: 100%;
      cursor: pointer;
    }
  }
}

[data-theme='dark'] .site-layout-content {
  background: #141414;
}
</style>

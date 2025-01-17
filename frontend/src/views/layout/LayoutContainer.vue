<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAcceptStore } from '@/stores/index.js'

const route = useRoute()
const router = useRouter()
const selectedKeys = ref([route.path])
const onChangeMenu = (page) => {
  if (selectedKeys.value === page) return

  router.push(page)
  selectedKeys.value = [page]
}

const open = ref(true)
const acceptStore = useAcceptStore()
const handleOk = (e) => {
  acceptStore.accept()
  open.value = false
}
const handleCancel = () => {
  window.open('', '_self').close()
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
          <a-menu-item key="/home" @click="onChangeMenu('/home')">文件列表</a-menu-item>
          <a-menu-item key="/about" @click="onChangeMenu('/about')">关于</a-menu-item>
        </a-menu>
      </a-layout-header>
      <a-layout-content>
        <router-view></router-view>
      </a-layout-content>

      <a-layout-footer style="text-align: center"> ZZHow </a-layout-footer>
    </a-layout>

    <a-modal
      v-model:open="open"
      title="MagicShare"
      style="width: auto"
      @ok="handleOk"
      @cancel="handleCancel"
      centered
      :maskClosable="false"
      :keyboard="false"
      :closable="false"
      cancelText="退出"
      okText="同意"
    >
      <p>使用本软件前，请仔细阅读：&#10;&#10;</p>
      <p>
        合法使用：
        本软件仅限于合法文件分享，严禁分享任何侵犯版权、涉及色情、暴力、欺诈、违法或其他有害内容的文件。&#10;
      </p>
      <p>个人责任： 您对分享内容的合法性负全部责任，请确保您拥有分享文件的合法授权。&#10;</p>
      <p>风险提示： 本软件无法保证所分享文件的安全性，请您自行检查文件的安全性。&#10;</p>
      <p>免责声明： 软件作者不对因使用本软件造成的任何直接或间接损失承担责任。</p>
    </a-modal>
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

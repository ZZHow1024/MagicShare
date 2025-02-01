<script setup>
import { onMounted, reactive, ref } from 'vue'
import { encryptRSA } from '@/utils/crypto.js'
import { message } from 'ant-design-vue'
import { useWSocketStore } from '@/stores/modules/wSocket.js'
import { useRouter } from 'vue-router'
import { useAcceptStore } from '@/stores/index.js'

// 表单数据
const formState = reactive({
  password: '',
})
const formRef = ref(null)
const onSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return e
  }

  pwdCheck(formState.password)
}

// 建立 WS 连接
const router = useRouter()
const wSocketStore = useWSocketStore()
const publicKey = ref('')
let wSocket = null
const connect = () => {
  // const hostname = window.location.hostname
  // const port = window.location.port
  // const webSocketUrl = 'ws://' + hostname + ':' + port + '/ws/connect'
  const webSocketUrl = 'ws://localhost:1024/ws/connect' //开发使用

  wSocket = new WebSocket(webSocketUrl) // 开发使用 WebSocket URL

  wSocket.onerror = () => {
    networkErrModelOpen.value = true
  }

  wSocket.onopen = () => {
    wSocket.send('ClientHello')

    wSocket.onmessage = async (event) => {
      if (event.data.startsWith('ServerHello#')) {
        publicKey.value = atob(event.data.split('#')[1])
      } else if (event.data.startsWith('Syn#')) {
        if (event.data.split('#')[1] === '200') {
          // 密码正确
          wSocketStore.setWSocket(wSocket)
          message.success('密码正确')
          router.replace('/home')
        } else {
          // 密码错误
          message.error('密码错误')
        }
      }
    }
  }
}

// 发起密码校验
const pwdCheck = async (pwd) => {
  if (publicKey.value === null || publicKey.value === '') {
    networkErrModelOpen.value = true
    return
  }

  if (wSocket === null || wSocket.readyState !== WebSocket.OPEN) {
    networkErrModelOpen.value = true
    return
  }

  const data = await encryptRSA(publicKey.value, pwd)
  wSocket.send('Syn#' + data)
}

// 连接异常提示框
const networkErrModelOpen = ref(false)
const networkErrHandleOk = () => {
  networkErrModelOpen.value = false
  connect()
}

// 用户许可协议弹窗
const acceptStore = useAcceptStore()
const promptOpen = ref(false)
onMounted(() => {
  promptOpen.value = !acceptStore.status
  if (acceptStore.status && wSocketStore.wSocket === null) networkErrModelOpen.value = true
})
const promptHandleOk = () => {
  connect()
  acceptStore.accept()
  promptOpen.value = false
}
const promptHandleCancel = () => {
  window.open('about:blank', '_self').close()
}
</script>

<template>
  <div class="login-page">
    <a-layout class="layout">
      <a-layout-header>
        <div class="logo">
          <span class="app-title">MagicShare</span>
          <span>
            <img
              class="logo-img"
              src="@/assets/MagicShare.png"
              height="488"
              width="522"
              alt="MagicShareLogo"
            />
          </span>
        </div>
      </a-layout-header>
      <a-layout-content>
        <div class="content-container" v-show="!promptOpen">
          <div class="content-title">请输入连接密码</div>
          <a-form
            ref="formRef"
            class="login-form"
            :model="formState"
            name="basic"
            :label-col="{ span: 8 }"
            :wrapper-col="{ span: 16 }"
            autocomplete="off"
          >
            <a-form-item
              label="连接密码："
              name="password"
              :rules="[
                { required: true, message: '请输入连接密码' },
                {
                  min: 3,
                  max: 10,
                  message: '长度应为 3-10 个字符',
                  trigger: 'blur',
                },
              ]"
            >
              <a-input-password v-model:value="formState.password" />
            </a-form-item>

            <a-form-item :wrapper-col="{ offset: 8, span: 16 }">
              <a-button type="primary" @click="onSubmit">验证</a-button>
            </a-form-item>
          </a-form>
        </div>
      </a-layout-content>

      <a-layout-footer style="text-align: center"> ZZHow </a-layout-footer>
    </a-layout>

    <a-modal
      v-model:open="networkErrModelOpen"
      title="连接断开"
      style="width: auto"
      @ok="networkErrHandleOk"
      :maskClosable="false"
      :keyboard="false"
      :closable="false"
      centered
      :cancel-button-props="{ style: { display: 'none' } }"
      okText="重新连接"
    >
      <strong>已取消分享</strong>或<strong>网络出现异常</strong>
    </a-modal>

    <a-modal
      v-model:open="promptOpen"
      title="MagicShare"
      style="width: auto"
      @ok="promptHandleOk"
      @cancel="promptHandleCancel"
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

.content-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;

  .content-title {
    font-size: 8vw;
    margin-bottom: 8vh;
  }

  .login-form {
    width: 100vw;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
  }
}
</style>

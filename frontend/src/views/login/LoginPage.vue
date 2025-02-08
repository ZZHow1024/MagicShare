<script setup>
import { onMounted, reactive, ref } from 'vue'
import { decryptRSA, decryptSha256, generateKeyPair } from '@/utils/crypto.js'
import { message } from 'ant-design-vue'
import { useWSocketStore } from '@/stores/modules/wSocket.js'
import { useRouter } from 'vue-router'
import { useAcceptStore } from '@/stores/index.js'
import { vueI18n } from '@/lang/index.js'
import { useI18n } from 'vue-i18n'

// 当前语言
const { locale } = useI18n()
const currentLanguage = ref(locale.value)

// 切换当前语言
const switchLanguage = () => {
  locale.value = currentLanguage.value
}

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
let wSocket = null
let sessionId = null
const connect = () => {
  // const hostname = window.location.hostname
  // const port = window.location.port
  // const webSocketUrl = 'ws://' + hostname + ':' + port + '/ws/connect'
  const webSocketUrl = 'ws://localhost:1024/ws/connect' //开发使用

  wSocket = new WebSocket(webSocketUrl) // 开发使用 WebSocket URL

  wSocket.onerror = () => {
    networkErrModelOpen.value = true
  }

  wSocket.onopen = async () => {
    const { publicKey, privateKey } = await generateKeyPair()
    wSocket.send('ClientHello#' + btoa(publicKey))

    wSocket.onmessage = async (event) => {
      if (event.data.startsWith('ServerHello#')) {
        sessionId = await decryptRSA(privateKey, event.data.split('#')[1])
        wSocketStore.setSessionId(sessionId)
      } else if (event.data.startsWith('Syn#')) {
        if (event.data.split('#')[1] === '200') {
          // 密码正确
          wSocketStore.setWSocket(wSocket)
          message.success(vueI18n.global.t('login.passwordIsCorrect'))
          router.replace('/home')
        } else if (event.data.split('#')[1] === '202') {
          // 无连接密码
          sessionId = await decryptRSA(privateKey, event.data.split('#')[2])
          wSocketStore.setSessionId(sessionId)
          wSocketStore.setWSocket(wSocket)
          router.replace('/home')
        } else {
          // 密码错误
          message.error(vueI18n.global.t('login.passwordIsIncorrect'))
        }
      }
    }
  }
}

// 发起密码校验
const pwdCheck = async (pwd) => {
  if (sessionId === null || sessionId === '') {
    networkErrModelOpen.value = true
    return
  }

  if (wSocket === null || wSocket.readyState !== WebSocket.OPEN) {
    networkErrModelOpen.value = true
    return
  }

  const data = decryptSha256(sessionId + pwd)
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
          <div class="content-title">{{ $t('login.title') }}</div>
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
              :label="$t('login.connectionPassword')"
              :label-col="{ span: 11 }"
              name="password"
              :rules="[
                { required: true, message: $t('login.passwordIsEmpty') },
                {
                  min: 3,
                  max: 10,
                  message: $t('login.passwordIsIllegal'),
                  trigger: 'blur',
                },
              ]"
            >
              <a-input-password v-model:value="formState.password" />
            </a-form-item>

            <a-form-item :wrapper-col="{ offset: 8, span: 16 }">
              <a-button type="primary" @click="onSubmit">{{ $t('login.button') }}</a-button>
            </a-form-item>
          </a-form>
        </div>
      </a-layout-content>

      <a-layout-footer style="text-align: center">
        <div class="footer">
          <span>ZZHow</span>
          <div class="language-container">
            <div>Language</div>
            <div>
              <a-select
                v-model:value="currentLanguage"
                style="width: 100px"
                @change="switchLanguage"
              >
                <a-select-option value="zh-Hans">简体中文</a-select-option>
                <a-select-option value="zh-Hant">繁體中文</a-select-option>
                <a-select-option value="en-US">English</a-select-option>
              </a-select>
            </div>
          </div>
        </div>
      </a-layout-footer>
    </a-layout>

    <a-modal
      v-model:open="networkErrModelOpen"
      :title="$t('message.disconnection.title')"
      style="width: auto"
      @ok="networkErrHandleOk"
      :maskClosable="false"
      :keyboard="false"
      :closable="false"
      centered
      :cancel-button-props="{ style: { display: 'none' } }"
      :okText="$t('message.disconnection.button')"
    >
      <strong>{{ $t('message.disconnection.content1') }}</strong
      >{{ $t('message.disconnection.content2')
      }}<strong>{{ $t('message.disconnection.content3') }}</strong>
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
      :cancelText="$t('prompt.exit')"
      :okText="$t('prompt.accept')"
    >
      <p>{{ $t('prompt.content1') }}</p>
      <p>{{ $t('prompt.content2') }}</p>
      <p>{{ $t('prompt.content3') }}</p>
      <p>{{ $t('prompt.content4') }}</p>
      <p>{{ $t('prompt.content5') }}</p>
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
    font-size: 5vw;
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

.footer {
  .language-container {
    position: fixed;
    right: 20px;
    bottom: 10px;
  }
}
</style>

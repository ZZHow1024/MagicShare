<script setup>
import { onMounted, reactive, ref } from 'vue'
import { encryptRSA } from '@/utils/crypto.js'
import { message } from 'ant-design-vue'

onMounted(() => {
  connect()
})

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

  console.log(formState.password)
  pwdCheck(formState.password)
}

// 建立 WS 连接
const publicKey = ref('')
let wSocket = null
const connect = () => {
  // const hostname = window.location.hostname
  // const port = window.location.port
  // const webSocketUrl = 'ws://' + hostname + ':' + port + '/ws/connect'
  const webSocketUrl = 'ws://localhost:1024/ws/connect' //开发使用

  wSocket = new WebSocket(webSocketUrl) // 开发使用 WebSocket URL

  wSocket.onopen = () => {
    wSocket.send('ClientHello')

    wSocket.onmessage = async (event) => {
      if (event.data.startsWith('ServerHello#')) {
        publicKey.value = atob(event.data.split('#')[1])
      } else if (event.data.startsWith('Syn#')) {
        if (event.data.split('#')[1] === '200') {
          // 密码正确
          message.success('提取码正确')
        } else {
          // 密码错误
          message.error('提取码错误')
        }
      }
    }
  }
}
// 发起密码校验
const pwdCheck = async (pwd) => {
  if (wSocket === null) return false

  const data = await encryptRSA(publicKey.value, pwd)
  wSocket.send('Syn#' + data)
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
        <div class="content-container">
          <div class="content-title">请输入文件提取码</div>
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
              label="提取码："
              name="password"
              :rules="[
                { required: true, message: '请输入提取码' },
                {
                  min: 3,
                  max: 10,
                  message: '长度为 3-10 个字符',
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

<script setup>
import { onMounted, ref } from 'vue'
import {
  decryptAES,
  decryptBufferAES,
  decryptRSA,
  encryptAES,
  encryptWAES,
  generateKeyPair,
} from '@/utils/crypto.js'
import forge from 'node-forge'
import { useWSocketStore } from '@/stores/index.js'
import { useRouter } from 'vue-router'

const columns = [
  {
    title: '文件信息',
    dataIndex: 'name',
    key: 'name',
    width: 'calc(35vw)',
  },
  {
    title: '路径',
    dataIndex: 'path',
    key: 'path',
    width: 'calc(25vw)',
  },
  {
    title: '操作',
    key: 'action',
    width: 'calc(25vw)',
    align: 'center',
  },
]
const data = ref([])
const count = ref(0)
const shareId = ref('')

const onDownloadFile = (record) => {
  const protocol = window.location.protocol
  const hostname = window.location.hostname
  const port = window.location.port
  window.location.href =
    protocol +
    '//' +
    hostname +
    ':' +
    port +
    `/api/download/${record.fileId}?shareId=${encodeURIComponent(shareId.value)}`
}

const open = ref(false)
const showDrawer = () => {
  open.value = true
}
const onClose = () => {
  open.value = false
}

// WS 连接状态管理
const router = useRouter()
const wSocketStore = useWSocketStore()
let wSocket = wSocketStore.wSocket
let wAesKey = wSocketStore.wAesKey
let wIv = wSocketStore.wIv
let sessionId = wSocketStore.sessionId
let downloadId = ''
onMounted(async () => {
  if (wSocket) {
    wSocket.onerror = () => {
      wSocketStore.clearWSocket()
      router.replace('/login')
    }
    wSocket.onclose = () => {
      wSocketStore.clearWSocket()
      router.replace('/login')
    }

    // 交换密钥
    let publicKey, privateKey
    if (wAesKey === null || wIv === null) {
      const keyPair = await generateKeyPair()
      publicKey = keyPair.publicKey
      privateKey = keyPair.privateKey
      wSocket.send('Exc#' + btoa(publicKey))
    } else wSocket.send('List#')

    wSocket.onmessage = async (event) => {
      if (event.data.startsWith('Exc#')) {
        wAesKey = await decryptRSA(privateKey, event.data.split('#')[1])
        wSocketStore.setWAesKey(wAesKey)
        wIv = atob(event.data.split('#')[2])
        wSocketStore.setWIv(wIv)
        wSocket.send('List#')
      } else if (event.data.startsWith('List#')) {
        if (wAesKey === null || wIv === null) return

        const decryptedData = decryptBufferAES(
          wAesKey,
          wIv,
          new Uint8Array(
            atob(event.data.split('#')[1])
              .split('')
              .map(function (c) {
                return c.charCodeAt(0)
              }),
          ),
        )

        let obj = JSON.parse(await decryptedData)

        shareId.value = obj.shareId
        count.value = obj.count
        data.value = obj.files
      } else if (event.data.startsWith('Download#')) {
        downloadId = atob(event.data.split('#')[1])
        startEncryptedDownload()
      }
    }
  }
})

// WS 加密传输相关
let socket = null

let fileId = ''
const chunks = ref([])
const fileName = ref('暂无')
const isEncryptedDownload = ref(false)
const downloadProgress = ref({
  connection: 0,
  totalBlock: 0,
  currentBlock: 0,
  decryptionBlock: 0,
})

// 加密下载任务被占用提示对话框
const modelOpen = ref(false)
const handleCancel = () => {
  modelOpen.value = false
}
const handleOk = () => {
  modelOpen.value = false //关闭对话框
  open.value = true //打开加密下载进度
}

const encryptedDownload = (record) => {
  if (isEncryptedDownload.value === true) {
    modelOpen.value = true
    return
  }
  isEncryptedDownload.value = true
  downloadProgress.value = {
    connection: 0,
    totalBlock: 0,
    currentBlock: 0,
    decryptionBlock: 0,
  }
  showDrawer()
  fileId = record.fileId
  fileName.value = record.name

  wSocket.send('Download#')
}

const startEncryptedDownload = () => {
  socket = new WebSocket('ws://localhost:1024/ws/download') // 测试使用

  downloadProgress.value.connection = 100

  fileId = encryptWAES(wAesKey, wIv, fileId)
  socket.onopen = () => {
    console.log(wSocketStore.sessionId)
    console.log(sessionId)
    socket.send('a,' + encryptWAES(wAesKey, wIv, sessionId + '#' + downloadId) + ',' + fileId)
  }

  socket.onmessage = async (event) => {
    if (event.data.startsWith('block#')) {
      const block = decryptAES(
        wAesKey,
        wIv,
        new Uint8Array(
          atob(event.data.split('#')[1])
            .split('')
            .map(function (c) {
              return c.charCodeAt(0)
            }),
        ),
      )
      downloadProgress.value.totalBlock = parseInt(block)

      socket.send('b,' + fileId)
    } else if (event.data.startsWith('fin')) {
      socket.close()
    } else {
      downloadProgress.value.currentBlock++
      const decryptedChunk = await decryptBufferAES(
        wAesKey,
        wIv,
        new Uint8Array(
          atob(event.data)
            .split('')
            .map(function (c) {
              return c.charCodeAt(0)
            }),
        ),
      )

      const decryptedChunkUint8 = new Uint8Array(decryptedChunk.length)
      for (let i = 0; i < decryptedChunkUint8.length; i++) {
        decryptedChunkUint8[i] = decryptedChunk.charCodeAt(i)
      }
      chunks.value.push(decryptedChunkUint8)
      downloadProgress.value.decryptionBlock++
      if (downloadProgress.value.decryptionBlock >= downloadProgress.value.totalBlock) mergeFiles()
    }
  }

  socket.onerror = (error) => {
    isEncryptedDownload.value = false
    console.error('WebSocket Error: ', error)
  }

  socket.onclose = () => {
    chunks.value = []
    isEncryptedDownload.value = false
  }
}

const mergeFiles = () => {
  // 在传输完成后，拼接所有解密后的块为一个大的 Uint8Array
  const finalDecryptedData = new Uint8Array(
    chunks.value.reduce((acc, chunk) => acc + chunk.length, 0),
  )

  let offset = 0
  for (const chunk of chunks.value) {
    finalDecryptedData.set(chunk, offset) // 将每个 chunk 拷贝至最终的 Uint8Array 中
    offset += chunk.length
  }

  const blob = new Blob([finalDecryptedData], { type: 'application/octet-stream' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName.value
  a.click()
}
</script>

<template>
  <div class="home-container">
    <br />
    <div style="width: 100vw">
      <span style="margin-left: 3vw; font-size: 20px">总文件数：{{ count }}</span>
      <a-button style="position: relative; margin-left: 36vw" type="primary" @click="showDrawer"
        >查看加密下载进度</a-button
      >
    </div>
    <div :style="{ background: '#fff', padding: '24px', minHeight: '280px' }">
      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="data"
        :locale="{ emptyText: '分享列表为空' }"
        :scroll="{ y: 'calc(60vh)' }"
      >
        <template #headerCell="{ column }">
          <template v-if="column.key === 'name'">
            <span> 文件信息 </span>
          </template>
        </template>

        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            {{ record.name }}<br />
            <a-tag :key="record.type" color="black"> {{ record.type }} </a-tag><br />
            {{ record.size }} KB
          </template>

          <template v-else-if="column.key === 'action'">
            <span>
              <a class="ant-dropdown-link" @click="onDownloadFile(record)"> 快速下载 </a>
              <br />
              <a class="ant-dropdown-link" @click="encryptedDownload(record)"> 加密下载 </a>
            </span>
          </template>
        </template>
      </a-table>

      <a-modal
        centered
        v-model:open="modelOpen"
        title="MagicShare 提示"
        cancel-text="取消"
        @cancel="handleCancel"
        ok-text="查看当前加密下载进度"
        @ok="handleOk"
      >
        加密下载任务被占用，不支持同时加密下载多个文件。
      </a-modal>
    </div>

    <a-drawer
      class="drawer"
      :width="500"
      title="加密下载进度"
      placement="bottom"
      :open="open"
      @close="onClose"
    >
      <template #extra>
        <a-button style="margin-right: 8px" @click="onClose">关闭</a-button>
      </template>

      <div>当前下载的文件：{{ fileName }}</div>
      <br />
      <div class="download-process-container">
        <div>
          <a-progress type="circle" :percent="downloadProgress.connection" />
          <div class="download-process-container">建立连接</div>
        </div>
        &nbsp;
        <div>
          <a-progress
            type="circle"
            :percent="
              Math.min(
                ((downloadProgress.currentBlock / downloadProgress.totalBlock) * 100).toFixed(1),
                100,
              )
            "
          />
          <div class="download-process-container">加密传输</div>
        </div>
        &nbsp;
        <div>
          <a-progress
            type="circle"
            :percent="
              Math.min(
                ((downloadProgress.decryptionBlock / downloadProgress.totalBlock) * 100).toFixed(1),
                100,
              )
            "
          />
          <div class="download-process-container">解密文件</div>
        </div>
      </div>
      <br />
      <div class="content-container">RSA + AES 混合加密</div>
      <div class="content-container">保障数据安全</div>
    </a-drawer>
  </div>
</template>

<style scoped lang="scss">
.drawer {
  .download-process-container {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .content-container {
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>

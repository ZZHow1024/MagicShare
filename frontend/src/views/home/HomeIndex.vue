<script setup>
import { computed, onMounted, ref } from 'vue'
import axios from 'axios'
import {
  base64ToBytes,
  binaryToBytes,
  decryptAES,
  decryptBufferAES,
  decryptRSA,
  encryptWAES,
  generateKeyPair,
} from '@/utils/crypto.js'
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

let quickDownloadList = []
const onDownloadFile = (record) => {
  quickDownloadList.push(record.fileId)
  wSocket.send('Download#0')
}

const startQuickDownload = () => {
  const protocol = window.location.protocol
  const hostname = window.location.hostname
  const port = window.location.port
  const data = {
    token: encryptWAES(wAesKey, wIv, sessionId + '#' + downloadId),
    shareId: encryptWAES(wAesKey, wIv, shareId.value),
    fileId: encryptWAES(wAesKey, wIv, quickDownloadList.pop()),
  }

  window.location.href =
    protocol +
    '//' +
    hostname +
    ':' +
    port +
    `/api/download?fileId=${encodeURIComponent(data.fileId)}&token=${encodeURIComponent(data.token)}&shareId=${encodeURIComponent(data.shareId)}`
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

        const decryptedData = await decryptAES(
          wAesKey,
          wIv,
          base64ToBytes(event.data.split('#')[1]),
        )

        let obj = JSON.parse(decryptedData)

        shareId.value = obj.shareId
        count.value = obj.count
        data.value = obj.files
      } else if (event.data.startsWith('Download#')) {
        downloadId = decryptAES(
          wAesKey,
          wIv,
          base64ToBytes(event.data.split('#')[2]),
        )
        if (event.data.split('#')[1] === '0') startQuickDownload()
        else startEncryptedDownload()
      } else if (event.data.startsWith('Archive#')) {
        if (pendingArchiveResolve === null) return
        const archiveId = decryptAES(
          wAesKey,
          wIv,
          base64ToBytes(event.data.split('#')[2]),
        )
        const resolveArchiveId = pendingArchiveResolve
        pendingArchiveResolve = null
        resolveArchiveId(archiveId)
      }
    }
  }
})

// WS 加密传输相关
let socket = null

let fileId = ''
const chunks = ref([])
const fileName = ref('-')
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

  wSocket.send('Download#1')
}

const startEncryptedDownload = () => {
  const hostname = window.location.hostname
  const port = window.location.port

  socket = new WebSocket('ws://' + hostname + ':' + port + '/ws/download')
  // socket = new WebSocket('ws://localhost:1024/ws/download')

  downloadProgress.value.connection = 100

  fileId = encryptWAES(wAesKey, wIv, fileId)
  socket.onopen = () => {
    socket.send('a,' + encryptWAES(wAesKey, wIv, sessionId + '#' + downloadId) + ',' + fileId)
  }

  socket.onmessage = async (event) => {
    if (event.data.startsWith('block#')) {
      const block = decryptAES(wAesKey, wIv, base64ToBytes(event.data.split('#')[1]))
      downloadProgress.value.totalBlock = parseInt(block)

      socket.send('b,' + fileId)
    } else if (event.data.startsWith('fin')) {
      socket.close()
    } else {
      // 加密分块（Base64 文本帧，每块随机 IV：IV || ciphertext）
      downloadProgress.value.currentBlock++
      const decryptedChunk = await decryptBufferAES(wAesKey, wIv, base64ToBytes(event.data))
      chunks.value.push(binaryToBytes(decryptedChunk))
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

// ===== 打包下载与在线预览 =====
const PREVIEWABLE_EXTENSIONS = [
  'txt', 'md', 'json', 'xml', 'log', 'csv', 'html', 'css', 'js', 'java', 'py',
  'png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp', 'svg',
  'mp4', 'webm', 'mov', 'mkv', 'avi',
  'mp3', 'wav', 'ogg', 'flac', 'm4a',
  'pdf',
]
const isPreviewable = (record) => {
  const name = String(record.name || '').toLowerCase()
  const dot = name.lastIndexOf('.')
  const ext = dot === -1 ? '' : name.slice(dot + 1)
  return PREVIEWABLE_EXTENSIONS.includes(ext)
}
const selectedRowKeys = ref([])
let pendingArchiveResolve = null
const selectedPaths = computed(() =>
  data.value.filter((file) => selectedRowKeys.value.includes(file.fileId)).map((file) => file.path),
)

const onSelectionChange = (keys) => {
  selectedRowKeys.value = keys
}

const requestArchiveId = () =>
  new Promise((resolve, reject) => {
    pendingArchiveResolve = resolve
    wSocket.send('Archive#0')
    setTimeout(() => {
      if (pendingArchiveResolve !== null) {
        pendingArchiveResolve = null
        reject(new Error('request archive id timeout'))
      }
    }, 10000)
  })

const archiveDownload = async () => {
  try {
    const archiveId = await requestArchiveId()
    const token = encryptWAES(wAesKey, wIv, sessionId + '#' + archiveId)
    const response = await axios.post(`/api/archive?token=${encodeURIComponent(token)}`, selectedPaths.value, {
      responseType: 'blob',
    })
    const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/zip' }))
    const a = document.createElement('a')
    a.href = url
    a.download = 'MagicShare-files.zip'
    a.click()
    window.URL.revokeObjectURL(url)
    selectedRowKeys.value = []
  } catch (error) {
    selectedRowKeys.value = []
  }
}

const previewFile = async (record) => {
  try {
    const archiveId = await requestArchiveId()
    const token = encryptWAES(wAesKey, wIv, sessionId + '#' + archiveId)
    const fileId = encryptWAES(wAesKey, wIv, record.fileId)
    window.open(
      `/api/preview?token=${encodeURIComponent(token)}&fileId=${encodeURIComponent(fileId)}`,
      '_blank',
    )
  } catch (error) {
    // 预览请求超时或失败时忽略
  }
}
</script>

<template>
  <div class="home-container">
    <br />
    <div style="width: 100vw">
      <span style="margin-left: 3vw; font-size: 20px">{{ $t('home.totalFiles') + count }}</span>
      <a-button style="position: relative; margin-left: 36vw" type="primary" @click="showDrawer">{{
        $t('home.button')
      }}</a-button>
      <a-button
        style="position: relative; margin-left: 8px"
        :disabled="selectedRowKeys.length === 0"
        @click="archiveDownload"
      >
        {{ $t('home.archiveDownload') + ' (' + selectedRowKeys.length + ')' }}
      </a-button>
    </div>
    <div :style="{ background: '#fff', padding: '24px', minHeight: '280px' }">
      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="data"
        :row-key="(record) => record.fileId"
        :row-selection="{ selectedRowKeys, onChange: onSelectionChange }"
        :locale="{ emptyText: $t('home.shareListIsEmpty') }"
        :scroll="{ y: 'calc(60vh)' }"
      >
        <template #headerCell="{ column }">
          <template v-if="column.key === 'name'">
            <span> {{ $t('home.fileInformation') }} </span>
          </template>
          <template v-if="column.key === 'path'">
            <span> {{ $t('home.path') }} </span>
          </template>
          <template v-if="column.key === 'action'">
            <span> {{ $t('home.operate') }} </span>
          </template>
        </template>

        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            {{ record.name }}<br />
            <a-tag :key="record.type" color="black"> {{ record.type }} </a-tag><br />
            {{ record.size }} KB
          </template>

          <template v-else-if="column.key === 'action'">
            <div>
              <a
                v-if="isPreviewable(record)"
                class="ant-dropdown-link"
                @click="previewFile(record)"
              >
                {{ $t('home.preview') }}
              </a>
              <div style="height: 20px">-----</div>
              <a class="ant-dropdown-link" @click="onDownloadFile(record)">
                {{ $t('home.quickDownload') }}
              </a>
              <div style="height: 20px">-----</div>
              <a class="ant-dropdown-link" @click="encryptedDownload(record)">
                {{ $t('home.encryptedDownloads') }}
              </a>
            </div>
          </template>
        </template>
      </a-table>

      <a-modal
        centered
        v-model:open="modelOpen"
        :title="$t('message.downloadBusy.title')"
        :cancel-text="$t('message.downloadBusy.cancelText')"
        @cancel="handleCancel"
        :ok-text="$t('message.downloadBusy.okText')"
        @ok="handleOk"
      >
        {{ $t('message.downloadBusy.content') }}
      </a-modal>
    </div>



    <a-drawer
      class="drawer"
      :width="500"
      :title="$t('home.drawer.title')"
      placement="bottom"
      :open="open"
      @close="onClose"
    >
      <template #extra>
        <a-button style="margin-right: 8px" @click="onClose">{{
          $t('home.drawer.close')
        }}</a-button>
      </template>

      <div>{{ $t('home.drawer.currentlyDownloadingFile') + fileName }}</div>
      <br />
      <div class="download-process-container">
        <div>
          <a-progress type="circle" :percent="downloadProgress.connection" />
          <div class="download-process-container">{{ $t('home.drawer.step1') }}</div>
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
          <div class="download-process-container">{{ $t('home.drawer.step2') }}</div>
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
          <div class="download-process-container">{{ $t('home.drawer.step3') }}</div>
        </div>
      </div>
      <br />
      <div class="content-container">{{ $t('home.drawer.footer.content1') }}</div>
      <div class="content-container">{{ $t('home.drawer.footer.content2') }}</div>
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

.upload-dragger {
  margin-top: 16px;
}

.upload-dragger-content {
  padding: 24px 0;
}

.upload-dragger-title {
  font-size: 16px;
  color: rgba(0, 0, 0, 0.88);
}

.upload-dragger-hint {
  margin-top: 8px;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.45);
}

.upload-task-title {
  font-weight: 500;
  margin-bottom: 12px;
}

.upload-task-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.upload-task-name {
  width: 30vw;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.upload-task-progress {
  flex: 1;
}

.upload-task-status {
  width: 90px;
  text-align: right;
  color: rgba(0, 0, 0, 0.45);
}
</style>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import { base64ToBytes, decryptAES, encryptWAES } from '@/utils/crypto.js'
import { useWSocketStore } from '@/stores/index.js'
import { useRouter } from 'vue-router'

// ===== 上传任务状态 =====
const CHUNK_SIZE = 4 * 1024 * 1024
const UPLOAD_STORAGE_KEY = 'magic-share-upload-tasks'
const uploadTasks = ref([])
const allowUpload = ref(true)
let pendingUploadResolve = null
const uploadQueue = []
const isProcessingQueue = ref(false)

const loadSavedTaskIds = () => {
  try {
    return JSON.parse(localStorage.getItem(UPLOAD_STORAGE_KEY)) || {}
  } catch {
    return {}
  }
}

const saveTaskId = (storageKey, taskId) => {
  const savedTaskIds = loadSavedTaskIds()
  savedTaskIds[storageKey] = taskId
  localStorage.setItem(UPLOAD_STORAGE_KEY, JSON.stringify(savedTaskIds))
}

const clearTaskId = (storageKey) => {
  const savedTaskIds = loadSavedTaskIds()
  delete savedTaskIds[storageKey]
  localStorage.setItem(UPLOAD_STORAGE_KEY, JSON.stringify(savedTaskIds))
}

const requestUploadId = () =>
  new Promise((resolve, reject) => {
    pendingUploadResolve = resolve
    wSocket.send('Upload#0')
    setTimeout(() => {
      if (pendingUploadResolve !== null) {
        pendingUploadResolve = null
        reject(new Error('request upload id timeout'))
      }
    }, 10000)
  })

const formatSize = (size) => {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  if (size < 1024 * 1024 * 1024) return (size / 1024 / 1024).toFixed(1) + ' MB'
  return (size / 1024 / 1024 / 1024).toFixed(1) + ' GB'
}

const uploadFile = async (task) => {
  task.statusKey = 'uploading'
  try {
    const uploadId = await requestUploadId()
    const token = encryptWAES(wAesKey, wIv, sessionId + '#' + uploadId)
    const storageKey = task.file.name + '#' + task.file.size
    const savedTaskIds = loadSavedTaskIds()
    const initResponse = await axios.post(`/api/upload/init?token=${encodeURIComponent(token)}`, {
      fileName: task.file.name,
      fileSize: task.file.size,
      chunkSize: CHUNK_SIZE,
      taskId: savedTaskIds[storageKey] || null,
    })
    const { taskId, totalChunks, receivedIndexes } = initResponse.data
    saveTaskId(storageKey, taskId)
    const received = new Set(receivedIndexes)
    for (let i = 0; i < totalChunks; i++) {
      if (!received.has(i)) {
        const chunk = task.file.slice(i * CHUNK_SIZE, (i + 1) * CHUNK_SIZE)
        await axios.post(
          `/api/upload/chunk?token=${encodeURIComponent(token)}&taskId=${taskId}&index=${i}`,
          chunk,
          { headers: { 'Content-Type': 'application/octet-stream' } },
        )
      }
      task.percent = Math.round(((i + 1) / totalChunks) * 100)
    }
    await axios.post(`/api/upload/merge?token=${encodeURIComponent(token)}&taskId=${taskId}`)
    clearTaskId(storageKey)
    task.statusKey = 'completed'
  } catch (error) {
    // 服务端关闭接收（403）时同步本地门控状态
    if (error?.response?.status === 403) allowUpload.value = false
    task.statusKey = 'failed'
  }
}

const processUploadQueue = async () => {
  if (isProcessingQueue.value) return
  isProcessingQueue.value = true
  while (uploadQueue.length > 0) {
    const task = uploadQueue.shift()
    await uploadFile(task)
  }
  isProcessingQueue.value = false
}

const beforeUpload = (file) => {
  const task = reactive({
    file,
    name: file.name,
    size: file.size,
    percent: 0,
    statusKey: 'waiting',
  })
  uploadTasks.value.push(task)
  uploadQueue.push(task)
  processUploadQueue()
  // 阻止 antd 默认上传行为，改用分片上传
  return false
}

// ===== WS 连接（仅处理上传相关消息）=====
const router = useRouter()
const wSocketStore = useWSocketStore()
let wSocket = wSocketStore.wSocket
let wAesKey = wSocketStore.wAesKey
let wIv = wSocketStore.wIv
let sessionId = wSocketStore.sessionId

onMounted(() => {
  if (!wSocket || wAesKey === null || wIv === null || sessionId === null) {
    wSocketStore.clearWSocket()
    router.replace('/login')
    return
  }

  wSocket.onerror = () => {
    wSocketStore.clearWSocket()
    router.replace('/login')
  }
  wSocket.onclose = () => {
    wSocketStore.clearWSocket()
    router.replace('/login')
  }

  // 门控：拉取接收开关状态（桌面端启动服务后状态即固定）
  axios.get('/api/upload/status').then((response) => {
    allowUpload.value = response.data.allowUpload === true
  })

  wSocket.onmessage = (event) => {
    if (event.data.startsWith('Upload#')) {
      if (pendingUploadResolve === null) return
      const uploadId = decryptAES(wAesKey, wIv, base64ToBytes(event.data.split('#')[2]))
      const resolveUploadId = pendingUploadResolve
      pendingUploadResolve = null
      resolveUploadId(uploadId)
    }
  }
})
</script>

<template>
  <div class="upload-container">
    <br />
    <div style="width: 100vw; text-align: center">
      <span style="font-size: 20px">{{ $t('home.upload.title') }}</span>
    </div>

    <div
      v-if="uploadTasks.length > 0"
      :style="{ background: '#fff', padding: '24px', margin: '16px 3vw' }"
    >
      <div class="upload-task-title">{{ $t('home.upload.title') }}</div>
      <div v-for="(task, index) in uploadTasks" :key="index" class="upload-task-row">
        <span class="upload-task-name">{{ task.name }}（{{ formatSize(task.size) }}）</span>
        <a-progress
          class="upload-task-progress"
          :percent="task.percent"
          size="small"
          :status="
            task.statusKey === 'failed' ? 'exception' : task.statusKey === 'completed' ? 'success' : 'active'
          "
        />
        <span class="upload-task-status">{{ $t('home.upload.' + task.statusKey) }}</span>
      </div>
    </div>

    <a-alert
      v-if="!allowUpload"
      type="warning"
      show-icon
      :message="$t('home.upload.notEnabled')"
      style="margin: 0 3vw 16px"
    />
    <div :style="{ background: '#fff', padding: '24px', margin: '0 3vw 16px' }">
      <a-upload-dragger multiple :disabled="!allowUpload" :before-upload="beforeUpload" :show-upload-list="false" class="upload-dragger">
        <div class="upload-dragger-content">
          <div class="upload-dragger-title">{{ $t('home.upload.title') }}</div>
          <div class="upload-dragger-hint">{{ $t('home.upload.hint') }}</div>
        </div>
      </a-upload-dragger>
    </div>
  </div>
</template>

<style scoped lang="scss">
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

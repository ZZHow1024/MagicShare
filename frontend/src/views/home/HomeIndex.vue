<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { checkCurrentShareService, getFileListService } from '@/api/file.js'
import { decryptAES, decryptBufferAES, decryptRSA, generateKeyPair } from '@/utils/crypto.js'
import forge from 'node-forge'

const columns = [
  {
    title: '文件名',
    dataIndex: 'name',
    key: 'name',
    width: 'calc(25vw)',
  },
  {
    title: '文件类型',
    dataIndex: 'type',
    key: 'type',
    width: 'calc(10vw)',
  },
  {
    title: '文件路径',
    dataIndex: 'path',
    key: 'path',
    width: 'calc(45vw)',
  },
  {
    title: '操作',
    key: 'action',
    width: 'calc(10vw)',
  },
]
const shareId = ref()
const count = ref(0)
const data = ref()
const timer = ref()

onMounted(() => {
  getFileList()
  timer.value = setInterval(() => {
    checkCurrentShare()
  }, 1000)
})

const checkCurrentShare = async () => {
  if (!shareId.value || shareId.value === '') {
    await getFileList()
    return
  }
  const res = await checkCurrentShareService(shareId.value).catch(() => {
    shareId.value = ''
    count.value = 0
    data.value = ''
  })
  if (res.data.data === false) await getFileList()
}

const getFileList = async () => {
  // 调用生成公私钥对
  let publicKey
  let privateKeyPem
  await generateKeyPair().then((keys) => {
    publicKey = keys.publicKey
    privateKeyPem = keys.privateKey
  })

  // 携带公钥发请求
  const res = await getFileListService(btoa(publicKey))

  const aseKey = await decryptRSA(privateKeyPem, res.data.data.key)
  const ivBase64 = res.data.data.iv
  const encryptedDataBase64 = res.data.data.data

  const decryptedData = decryptAES(
    aseKey,
    forge.util.decode64(ivBase64),
    forge.util.decode64(encryptedDataBase64),
  )

  let obj = JSON.parse(decryptedData)

  shareId.value = obj.shareId
  count.value = obj.count
  data.value = obj.files
}

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

onBeforeUnmount(() => {
  clearInterval(timer.value)
})

const downloadEncryptedFile = async () => {
  // 调用生成公私钥对
  let publicKey
  let privateKeyPem
  await generateKeyPair().then((keys) => {
    publicKey = keys.publicKey
    privateKeyPem = keys.privateKey
  })

  // 携带公钥发请求
  const response = await fetch('http://localhost:1024/api/download/download', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    mode: 'cors',
    body: JSON.stringify(btoa(publicKey)),
  })
  const encryptedAesKey = response.headers.get('X-Aes-Key')
  const iv = response.headers.get('X-Aes-Iv')
  const aesKey = await decryptRSA(privateKeyPem, encryptedAesKey) // 使用私钥解密AES密钥

  const reader = response.body.getReader()

  let done, value
  let result = ''
  while (({ done, value } = await reader.read())) {
    if (done) {
      break
    }

    const decryptedChunk = await decryptBufferAES(
      aesKey,
      forge.util.decode64(iv),
      new Uint8Array(value),
    ) // 使用AES解密文件块
    result += decryptedChunk
  }

  // 拼接所有解密后的块
  const finalDecryptedData = forge.util.createBuffer(result)

  // 创建 Blob 进行下载或保存为文件
  const byteArray = new Uint8Array(
    finalDecryptedData
      .getBytes()
      .split('')
      .map((char) => char.charCodeAt(0)),
  )

  const blob = new Blob([byteArray], { type: 'application/octet-stream' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'download.png'
  a.click()
}
</script>

<template>
  <div class="home-container">
    <br />
    <div style="margin-left: 3vw; font-size: 20px">总文件数：{{ count }}</div>
    <a-button @click="downloadEncryptedFile">test</a-button>
    <div :style="{ background: '#fff', padding: '24px', minHeight: '280px' }">
      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="data"
        :locale="{ emptyText: '暂无分享的文件' }"
        :scroll="{ y: 'calc(60vh)' }"
      >
        <template #headerCell="{ column }">
          <template v-if="column.key === 'name'">
            <span>
              <smile-outlined />
              文件名
            </span>
          </template>
        </template>

        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a @click="onDownloadFile(record)">
              {{ record.name }}
            </a>
          </template>

          <template v-else-if="column.key === 'type'">
            <span>
              <a-tag :key="record.type" color="black">
                {{ record.type }}
              </a-tag>
            </span>
          </template>

          <template v-else-if="column.key === 'action'">
            <span>
              <a class="ant-dropdown-link" @click="onDownloadFile(record)"> 下载文件 </a>
            </span>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<style scoped lang="scss"></style>

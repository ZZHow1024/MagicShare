<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { getFileListService } from '@/api/file.js'

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
const data = ref()
const shareId = ref()
const timer = ref()
const count = ref(0)

onMounted(() => {
  getFileList()
  timer.value = setInterval(() => {
    getFileList()
  }, 1000)
})

const getFileList = async () => {
  const res = await getFileListService()
  shareId.value = res.data.data.shareId
  count.value = res.data.data.count
  data.value = res.data.data.files
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
</script>

<template>
  <div class="home-container">
    <br />
    <div style="margin-left: 3vw; font-size: 20px">总文件数：{{ count }}</div>
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

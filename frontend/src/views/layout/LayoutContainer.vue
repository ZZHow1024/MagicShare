<script setup>
import { onMounted, ref } from 'vue'
import { getFileListService } from '@/api/file.js'
const selectedKeys = ref(['1'])

const columns = [
  {
    title: '文件名',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '文件类型',
    dataIndex: 'type',
    key: 'type',
  },
  {
    title: '文件路径',
    dataIndex: 'path',
    key: 'path',
  },
  {
    title: '操作',
    key: 'action',
  },
]
const data = ref([
  {
    id: 1,
    name: 'aaa',
    type: 'png',
    path: '/home',
  },
  {
    id: 2,
    name: 'bbb',
    type: 'jpg',
    path: '/home',
  },
  {
    id: 3,
    name: 'ccc',
    type: 'mp3',
    path: '/home',
  },
])

onMounted(async () => {
  const res = await getFileListService()
  data.value = res.data.data
  console.log(res.data.data)
})
</script>

<template>
  <div class="layout-container">
    <a-layout class="layout">
      <a-layout-header>
        <div class="logo" />
        <a-menu
          v-model:selectedKeys="selectedKeys"
          theme="dark"
          mode="horizontal"
          :style="{ lineHeight: '64px' }"
        >
          <a-menu-item key="1">文件浏览</a-menu-item>
          <a-menu-item key="2">关于</a-menu-item>
        </a-menu>
        <span class="app-title">MagicShare 1.0.0</span>
      </a-layout-header>
      <a-layout-content style="padding: 0 50px">
        <a-breadcrumb style="margin: 16px 0">
          <a-breadcrumb-item>File</a-breadcrumb-item>
          <a-breadcrumb-item>List</a-breadcrumb-item>
        </a-breadcrumb>
        <div :style="{ background: '#fff', padding: '24px', minHeight: '280px' }">
          <!-- 表格 -->
          <a-table :columns="columns" :data-source="data">
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
                <a>
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
                  <a class="ant-dropdown-link"> 下载文件 </a>
                </span>
              </template>
            </template>
          </a-table>
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

.app-title {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  text-align: center;
  font-size: 2.5vw;
  color: white;
}

.site-layout-content {
  min-height: 280px;
  padding: 24px;
  background: #fff;
}
#components-layout-demo-top .logo {
  float: left;
  width: 120px;
  height: 31px;
  margin: 16px 24px 16px 0;
  background: rgba(255, 255, 255, 0.3);
}
.ant-row-rtl #components-layout-demo-top .logo {
  float: right;
  margin: 16px 0 16px 24px;
}

[data-theme='dark'] .site-layout-content {
  background: #141414;
}
</style>

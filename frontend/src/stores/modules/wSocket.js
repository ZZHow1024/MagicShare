import { defineStore } from 'pinia'
import { ref } from 'vue'

// WS 连接模块
export const useWSocketStore = defineStore('magic-share-w-socket', () => {
  const wSocket = ref(null)

  const setWSocket = (w) => {
    wSocket.value = w
  }

  const clearWSocket = () => {
    wSocket.value = null
  }

  return {
    wSocket,
    setWSocket,
    clearWSocket,
  }
})

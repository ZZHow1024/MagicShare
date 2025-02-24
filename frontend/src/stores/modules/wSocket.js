import { defineStore } from 'pinia'
import { ref } from 'vue'

// WS 连接模块
export const useWSocketStore = defineStore('magic-share-w-socket', () => {
  const wSocket = ref(null)
  const wAesKey = ref(null)
  const wIv = ref(null)
  const sessionId = ref(null)

  const setWSocket = (w) => {
    wSocket.value = w
  }

  const setWAesKey = (key) => {
    wAesKey.value = key
  }

  const setWIv = (i) => {
    wIv.value = i
  }

  const setSessionId = (id) => {
    sessionId.value = id
  }

  const clearWSocket = () => {
    wSocket.value = null
    wAesKey.value = null
    wIv.value = null
    sessionId.value = null
  }

  return {
    wSocket,
    wAesKey,
    wIv,
    sessionId,
    setWSocket,
    setWAesKey,
    setWIv,
    setSessionId,
    clearWSocket,
  }
})

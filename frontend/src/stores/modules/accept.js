import { defineStore } from 'pinia'
import { ref } from 'vue'

// 用户模块
export const useAcceptStore = defineStore('magic-share-accept', () => {
  const status = ref(false)
  const accept = async () => {
    status.value = true
  }

  return {
    status,
    accept,
  }
})

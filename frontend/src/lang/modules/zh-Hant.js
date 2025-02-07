export default {
  prompt: {
    content1: '使用本軟體前，請仔細閱讀：\n\n',
    content2:
      '合法使用： 本軟體僅限於合法文件分享，嚴禁分享任何侵犯版權、涉及色情、暴力、詐欺、違法或其他有害內容的文件。\n',
    content3: '個人責任： 您對分享內容的合法性負全部責任，請確保您擁有分享文件的合法授權。\n',
    content4: '風險提示： 本軟體無法保證所分享文件的安全性，請您自行檢查文件的安全性。\n',
    content5: '免責聲明： 軟體作者不對因使用本軟體造成的任何直接或間接損失負責。',
    exit: '退出',
    accept: '接受',
  },
  message: {
    disconnection: {
      title: '連線中斷',
      content1: '已取消分享',
      content2: ' 或 ',
      content3: '網路出現異常',
      button: '重新連接',
    },
  },
  login: {
    title: '請輸入連線密碼',
    connectionPassword: '連線密碼：',
    passwordIsEmpty: '連線密碼不能為空',
    passwordIsIllegal: '連接密碼長度應為3-10個字符',
    button: '驗證',
    passwordIsCorrect: '密碼正確',
    passwordIsIncorrect: '密碼錯誤',
  },
  home: {
    title: '分享清單',
    close: '關閉',
    totalFiles: '總檔案數：',
    button: '查看加密下載進度',
    fileInformation: '檔案訊息',
    path: '路徑',
    operate: '操作',
    shareListIsEmpty: '分享清單為空',
    quickDownload: '快速下載',
    encryptedDownloads: '加密下載',
    drawer: {
      title: '加密下載進度',
      currentlyDownloadingFile: '目前下載的檔案：',
      none: '暫無',
      step1: '建立連線',
      step2: '加密傳輸',
      step3: '解密檔案',
      footer: {
        content1: 'RSA + AES 混合加密',
        content2: '保障資料安全',
      },
    },
  },
  about: {
    title: '關於',
  },
}

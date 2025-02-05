export default {
  prompt: {
    content:
      '<p>使用本軟體前，請仔細閱讀：&#10;&#10;</p>\n' +
      '<p>合法使用： 本軟體僅限於合法文件分享，嚴禁分享任何侵犯版權、涉及色情、暴力、詐欺、違法或其他有害內容的文件。&#10;</p>\n' +
      '<p>個人責任： 您對分享內容的合法性負全部責任，請確保您擁有分享文件的合法授權。&#10;</p>\n' +
      '<p>風險提示： 本軟體無法保證所分享文件的安全性，請您自行檢查文件的安全性。&#10;</p>\n' +
      '<p>免責聲明： 軟體作者不對因使用本軟體造成的任何直接或間接損失負責。</p>',
    exit: '退出',
    accept: '接受',
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
  message: {
    disconnection: {
      title: '連線中斷',
      content: '<strong>已取消分享</strong> 或 <strong>網路出現異常</strong>',
      button: '重新連接',
    },
  },
}

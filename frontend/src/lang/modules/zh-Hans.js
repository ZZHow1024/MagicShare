export default {
  prompt: {
    content1: '使用本软件前，请仔细阅读：\n\n',
    content2:
      '合法使用：本软件仅限于合法文件分享，严禁分享任何侵犯版权、涉及色情、暴力、欺诈、违法或其他有害内容的文件。\n',
    content3: '个人责任： 您对分享内容的合法性负全部责任，请确保您拥有分享文件的合法授权。\n',
    content4: '风险提示： 本软件无法保证所分享文件的安全性，请您自行检查文件的安全性。\n',
    content5: '免责声明： 软件作者不对因使用本软件造成的任何直接或间接损失承担责任。',
    exit: '退出',
    accept: '同意',
  },
  message: {
    disconnection: {
      title: '连接断开',
      content1: '已取消分享',
      content2: ' 或 ',
      content3: '网络出现异常',
      button: '重新连接',
    },
  },
  login: {
    title: '请输入连接密码',
    connectionPassword: '连接密码：',
    passwordIsEmpty: '连接密码不能为空',
    passwordIsIllegal: '连接密码长度应为3-10个字符',
    button: '验证',
    passwordIsCorrect: '密码正确',
    passwordIsIncorrect: '密码错误',
  },
  home: {
    title: '分享列表',
    close: '关闭',
    totalFiles: '总文件数：',
    button: '查看加密下载进度',
    fileInformation: '文件信息',
    path: '路径',
    operate: '操作',
    shareListIsEmpty: '分享列表为空',
    quickDownload: '快速下载',
    encryptedDownloads: '加密下载',
    drawer: {
      title: '加密下载进度',
      currentlyDownloadingFile: '当前下载的文件：',
      none: '暂无',
      step1: '建立连接',
      step2: '加密传输',
      step3: '解密文件',
      footer: {
        content1: 'RSA + AES 混合加密',
        content2: '保障数据安全',
      },
    },
  },
  about: {
    title: '关于',
  },
}

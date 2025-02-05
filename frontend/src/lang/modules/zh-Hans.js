export default {
  prompt: {
    content:
      '<p>使用本软件前，请仔细阅读：&#10;&#10;</p>\n' +
      '<p>合法使用：本软件仅限于合法文件分享，严禁分享任何侵犯版权、涉及色情、暴力、欺诈、违法或其他有害内容的文件。&#10;</p>\n' +
      '<p>个人责任： 您对分享内容的合法性负全部责任，请确保您拥有分享文件的合法授权。&#10;</p>\n' +
      '<p>风险提示： 本软件无法保证所分享文件的安全性，请您自行检查文件的安全性。&#10;</p>\n' +
      '<p>免责声明： 软件作者不对因使用本软件造成的任何直接或间接损失承担责任。</p>',
    exit: '退出',
    accept: '同意',
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
  message: {
    disconnection: {
      title: '连接断开',
      content: '<strong>已取消分享</strong> 或 <strong>网络出现异常</strong>',
      button: '重新连接',
    },
  },
}

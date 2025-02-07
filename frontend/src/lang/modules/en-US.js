export default {
  prompt: {
    content1: 'Please read carefully before using this software:\n\n',
    content2:
      'Legal use: This software is limited to legal file sharing. It is strictly forbidden to share any files that infringe copyright, involve pornography, violence, fraud, illegal or other harmful content.\n',
    content3:
      'Personal responsibility: You are fully responsible for the legality of the shared content. Please make sure that you have the legal authorization to share the file.\n',
    content4:
      'Risk warning: This software cannot guarantee the security of the shared files. Please check the security of the files yourself.\n',
    content5:
      'Disclaimer: The software author is not responsible for any direct or indirect losses caused by the use of this software.',
    exit: 'Exit',
    accept: 'Accept',
  },
  message: {
    disconnection: {
      title: 'Disconnection',
      content1: 'Share canceled',
      content2: ' or ',
      content3: 'Network abnormality',
      button: 'Reconnect',
    },
  },
  login: {
    title: 'Please enter the connection password',
    connectionPassword: 'Connection password:',
    passwordIsEmpty: 'The connection password cannot be empty',
    passwordIsIllegal: 'The connection password should be 3-10 characters long',
    button: 'Verify',
    passwordIsCorrect: 'Password is correct',
    passwordIsIncorrect: 'Password is incorrect',
  },
  home: {
    title: 'Share list',
    totalFiles: 'Total number of files: ',
    button: 'Decryption download progress',
    fileInformation: 'File information',
    path: 'Path',
    operate: 'Operate',
    shareListIsEmpty: 'Share list is empty',
    quickDownload: 'Quick download',
    encryptedDownloads: 'Encrypted downloads',
    drawer: {
      title: 'Encrypted download progress',
      close: 'close',
      currentlyDownloadingFile: 'Currently downloading file: ',
      none: 'None',
      step1: 'Establishing connection',
      step2: 'Encrypted transmission',
      step3: 'Decrypting Files',
      footer: {
        content1: 'RSA + AES hybrid encryption',
        content2: 'Ensure data security',
      },
    },
  },
  about: {
    title: 'About',
  },
}

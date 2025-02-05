export default {
  prompt: {
    content:
      '<p>Please read carefully before using this software:&#10;&#10;</p>\n' +
      '<p>Legal use: This software is limited to legal file sharing. It is strictly forbidden to share any files that infringe copyright, involve pornography, violence, fraud, illegal or other harmful content.&#10;</p>\n' +
      '<p>Personal responsibility: You are fully responsible for the legality of the shared content. Please make sure that you have the legal authorization to share the file.&#10;</p>\n' +
      '<p>Risk warning: This software cannot guarantee the security of the shared files. Please check the security of the files yourself.&#10;</p>\n' +
      '<p>Disclaimer: The software author is not responsible for any direct or indirect losses caused by the use of this software.</p>',
    exit: 'Exit',
    accept: 'Accept',
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
  message: {
    disconnection: {
      title: 'Disconnection',
      content: '<strong>Share canceled</strong> or <strong>Network abnormality</strong>',
      button: 'Reconnect',
    },
  },
}

import forge from 'node-forge'

// 生成公私钥对
const generateKeyPair = async () => {
  const keyPair = await window.crypto.subtle.generateKey(
    {
      name: 'RSA-OAEP',
      modulusLength: 2048,
      publicExponent: new Uint8Array([0x01, 0x00, 0x01]),
      hash: { name: 'SHA-256' },
    },
    true, // 是否可以导出密钥
    ['encrypt', 'decrypt'],
  )

  // 导出公钥和私钥为 PEM 格式
  const publicKey = await window.crypto.subtle.exportKey('spki', keyPair.publicKey)
  const privateKey = await window.crypto.subtle.exportKey('pkcs8', keyPair.privateKey)

  return {
    publicKey: arrayBufferToPem(publicKey, 'PUBLIC KEY'),
    privateKey: arrayBufferToPem(privateKey, 'PRIVATE KEY'),
  }
}

// 将 ArrayBuffer 转换为 PEM 格式
const arrayBufferToPem = (buffer, type) => {
  const base64String = btoa(String.fromCharCode(...new Uint8Array(buffer)))
  const formatted = base64String.match(/.{1,64}/g).join('\n')
  return `-----BEGIN ${type}-----\n${formatted}\n-----END ${type}-----`
}

// RSA 加密
const encryptRSA = (publicKeyPem, aesKey) => {
  const publicKey = forge.pki.publicKeyFromPem(publicKeyPem)

  const encrypted = publicKey.encrypt(aesKey, 'RSA-OAEP', {
    md: forge.md.sha256.create(),
    mgf1: {
      md: forge.md.sha1.create(),
    },
  })

  return forge.util.encode64(encrypted) // Base64 编码加密后的数据
}

// RSA 解密
const decryptRSA = async (privateKeyPem, encryptedBase64) => {
  const privateKey = forge.pki.privateKeyFromPem(privateKeyPem)
  const encryptedData = forge.util.decode64(encryptedBase64)

  const decrypted = privateKey.decrypt(encryptedData, 'RSA-OAEP', {
    md: forge.md.sha256.create(),
    mgf1: {
      md: forge.md.sha1.create(),
    },
  })

  return decrypted
}

// ASE 加密
const encryptASE = (data) => {
  // 生成 AES 密钥
  const aesKey = forge.random.getBytesSync(32) // 32 字节 = 256 位密钥
  const iv = forge.random.getBytesSync(16) // 16 字节的 IV（初始化向量）

  // 加密数据
  const cipher = forge.cipher.createCipher('AES-CBC', aesKey)
  cipher.start({ iv: iv })
  cipher.update(forge.util.createBuffer(data))
  cipher.finish()

  // 获取加密结果
  const encryptedData = cipher.output
  const encryptedBase64 = forge.util.encode64(encryptedData.getBytes())
  const aesKeyBase64 = forge.util.encode64(aesKey)
  const ivBase64 = forge.util.encode64(iv)

  return {
    aesKeyBase64: aesKeyBase64,
    encryptedDataBase64: encryptedBase64,
    iVBase64: ivBase64,
  }
}

// ASE 解密
const decryptASE = (aesKey, ivBase64, encryptedBase64) => {
  // 解码 Base64 编码的加密数据和 IV
  const iv = forge.util.decode64(ivBase64)
  const encryptedData = forge.util.decode64(encryptedBase64)

  // 解密数据
  const decipher = forge.cipher.createDecipher('AES-CBC', aesKey)
  decipher.start({ iv: iv })
  decipher.update(forge.util.createBuffer(encryptedData))
  const success = decipher.finish()

  if (success) {
    const decryptedData = decipher.output.toString()
    return decryptedData
  } else {
    return null
  }
}

export { generateKeyPair, encryptRSA, decryptRSA, encryptASE, decryptASE }

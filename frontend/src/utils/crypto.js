import forge from 'node-forge'

// 生成公私钥对
export const generateKeyPair = async () => {
  // 创建 RSA 密钥对
  const rsa = forge.pki.rsa
  const keyPair = await new Promise((resolve, reject) => {
    rsa.generateKeyPair({ bits: 2048, e: 0x10001 }, (err, keyPair) => {
      if (err) reject(err)
      resolve(keyPair)
    })
  })

  // 导出公钥和私钥为 PEM 格式
  const publicKey = forge.pki.publicKeyToPem(keyPair.publicKey)
  const privateKey = forge.pki.privateKeyToPem(keyPair.privateKey)

  return {
    publicKey,
    privateKey,
  }
}

// RSA 加密
export const encryptRSA = (publicKeyPem, data) => {
  const publicKey = forge.pki.publicKeyFromPem(publicKeyPem)
  const encrypted = publicKey.encrypt(data, 'RSA-OAEP', {
    md: forge.md.sha256.create(),
    mgf1: {
      md: forge.md.sha256.create(),
    },
  })

  return forge.util.encode64(encrypted) // Base64 编码加密后的数据
}

// RSA 解密
export const decryptRSA = async (privateKeyPem, encryptedBase64) => {
  const privateKey = forge.pki.privateKeyFromPem(privateKeyPem)
  const encryptedData = forge.util.decode64(encryptedBase64)

  const decrypted = privateKey.decrypt(encryptedData, 'RSA-OAEP', {
    md: forge.md.sha256.create(),
    mgf1: {
      md: forge.md.sha256.create(),
    },
  })

  return decrypted
}

// AES 加密
export const encryptAES = (data) => {
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

// AES 加密（每消息随机 IV，输出 Base64(IV || ciphertext)，与后端协议一致）
export const encryptWAES = (aesKey, iv, data) => {
  // 生成随机 IV（iv 参数保留以兼容旧调用，不再使用固定 IV）
  const randomIv = forge.random.getBytesSync(16)

  const cipher = forge.cipher.createCipher('AES-CBC', aesKey)
  cipher.start({ iv: randomIv })
  cipher.update(forge.util.createBuffer(data))
  cipher.finish()

  // 组合 IV 与密文：Base64(IV || ciphertext)
  const ivBinary = forge.util.decode64(forge.util.encode64(randomIv))
  const cipherBinary = cipher.output.getBytes()

  return forge.util.encode64(ivBinary + cipherBinary)
}

// AES 解密（输入 Uint8Array，格式为 IV || ciphertext，与后端协议一致；iv 参数保留以兼容旧调用）
export const decryptAES = (aesKey, iv, encryptedData) => {
  if (!encryptedData || encryptedData.length <= 16) {
    console.error('Decryption failed: ciphertext too short')
    return null
  }

  const decipher = forge.cipher.createDecipher('AES-CBC', aesKey)
  decipher.start({ iv: forge.util.createBuffer(bytesToBinary(encryptedData.slice(0, 16))) })
  decipher.update(forge.util.createBuffer(bytesToBinary(encryptedData.slice(16))))
  const success = decipher.finish()

  if (success) {
    return decipher.output.toString()
  } else {
    console.error('Decryption failed')
    return null
  }
}

// AES 解密（输入 Uint8Array，格式为 IV || ciphertext，返回二进制字符串；iv 参数保留以兼容旧调用）
export const decryptBufferAES = async (aesKey, iv, encryptedData) => {
  if (!encryptedData || encryptedData.length <= 16) {
    console.error('Decryption failed: ciphertext too short')
    return null
  }

  const cipher = forge.cipher.createDecipher('AES-CBC', aesKey)
  cipher.start({ iv: forge.util.createBuffer(bytesToBinary(encryptedData.slice(0, 16))) })
  cipher.update(forge.util.createBuffer(bytesToBinary(encryptedData.slice(16))))
  const result = await cipher.finish()

  if (result) {
    return cipher.output.getBytes()
  } else {
    console.error('Decryption failed')
    return null
  }
}

// Base64 -> Uint8Array（比 atob().split().map() 快约一个数量级）
export const base64ToBytes = (base64) => {
  const binary = atob(base64)
  const bytes = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i)
  return bytes
}

// 二进制字符串 -> Uint8Array
export const binaryToBytes = (binary) => {
  const bytes = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i)
  return bytes
}

// Uint8Array -> 二进制字符串
const bytesToBinary = (bytes) => {
  let binary = ''
  for (let i = 0; i < bytes.length; i++) binary += String.fromCharCode(bytes[i])
  return binary
}

// SHA256 加密
export const decryptSha256 = (data) => {
  const sha256 = forge.md.sha256.create()
  sha256.update(data)

  return forge.util.encode64(sha256.digest().bytes())
}

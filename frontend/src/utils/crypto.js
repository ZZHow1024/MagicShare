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
export const encryptRSA = (publicKeyPem, aesKey) => {
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
export const decryptRSA = async (privateKeyPem, encryptedBase64) => {
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
export const encryptASE = (data) => {
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
export const decryptASE = (aesKey, ivBase64, encryptedBase64) => {
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

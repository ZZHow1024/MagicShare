import forge from 'node-forge'

// 安全上下文（localhost / https）下 crypto.subtle 可用，node-forge 会自动走 Web Crypto 原生路径（毫秒级）。
// 非安全上下文（如 http + 裸 IP 访问）缺少 crypto.subtle，node-forge 回退到纯 JS 路径时会创建
// prime.worker.js Web Worker——Vite 打包产物中不存在该文件，Worker 404 后 forge 既不报错也不回退，
// 导致密钥生成静默挂起。因此非安全上下文改用本文件的快速实现（实测 ~0.6s）：
// jsbn 素数搜索（含小素数试除筛，复刻 node-forge prime.js 的 PRIMEINC 算法）+ forge 密钥组装，
// 并通过时间预算化让步保持页面响应。
const hasSubtleCrypto = typeof crypto !== 'undefined' && typeof crypto.subtle === 'object'

// Miller-Rabin 轮次表（HAC 4.4 表，素性错误概率 (1/2)^80），与 node-forge prime.js 一致
const millerRabinTests = (numBits) =>
  numBits <= 100 ? 27
  : numBits <= 150 ? 18
  : numBits <= 200 ? 15
  : numBits <= 250 ? 12
  : numBits <= 300 ? 9
  : numBits <= 350 ? 8
  : numBits <= 400 ? 7
  : numBits <= 500 ? 6
  : numBits <= 600 ? 5
  : numBits <= 800 ? 4
  : numBits <= 1250 ? 3
  : 2

// 非安全上下文下的 RSA 密钥对生成
const generateKeyPairByJsbn = async (bits = 2048) => {
  const BigInteger = forge.jsbn.BigInteger
  const ONE = BigInteger.ONE
  const THIRTY = new BigInteger(null)
  THIRTY.fromInt(30)
  // 素数均为 30k+i（i ∈ {1,7,11,13,17,19,23,29}），候选步进
  const GCD_30_DELTA = [6, 4, 2, 4, 2, 4, 6, 2]
  const opOr = (x, y) => x | y
  const rng = {
    nextBytes: (array) => {
      const bytes = forge.random.getBytesSync(array.length)
      for (let i = 0; i < array.length; i++) array[i] = bytes.charCodeAt(i) & 0xff
    },
  }

  // 与 node-forge prime.js generateRandom 相同：强制最高位置位并对齐 30k+1
  const generateRandom = (numBits) => {
    const num = new BigInteger(numBits, rng)
    const topBit = numBits - 1
    if (!num.testBit(topBit)) num.bitwiseTo(ONE.shiftLeft(topBit), opOr, num)
    num.dAddOffset(31 - num.mod(THIRTY).byteValue(), 0)
    return num
  }

  // 每 40ms 让出主线程一次（forge.util.setImmediate 走 MessageChannel，不受 setTimeout 钳制影响）
  const yieldToUI = () => new Promise((resolve) => forge.util.setImmediate(resolve))

  const findPrime = async (numBits, tests) => {
    let num = generateRandom(numBits)
    let deltaIdx = 0
    let chunkStart = Date.now()
    for (;;) {
      // 溢出则重新生成随机数
      if (num.bitLength() > numBits) {
        num = generateRandom(numBits)
        deltaIdx = 0
      }
      if (num.isProbablePrime(tests)) return num
      num.dAddOffset(GCD_30_DELTA[deltaIdx++ % 8], 0)
      if (Date.now() - chunkStart > 40) {
        await yieldToUI()
        chunkStart = Date.now()
      }
    }
  }

  const e = new BigInteger(null)
  e.fromInt(0x10001)
  const halfBits = bits >> 1
  const tests = millerRabinTests(halfBits)
  let p = null
  let q = null

  // 与 node-forge rsa.js finish() 相同的组装校验循环
  for (;;) {
    if (p === null) p = await findPrime(halfBits, tests)
    if (q === null) q = await findPrime(halfBits, tests)

    // 保证 p > q
    if (p.compareTo(q) < 0) {
      const tmp = p
      p = q
      q = tmp
    }

    // 保证 p、q 与 e 互素，不满足则重新生成
    if (p.subtract(ONE).gcd(e).compareTo(ONE) !== 0) {
      p = null
      continue
    }
    if (q.subtract(ONE).gcd(e).compareTo(ONE) !== 0) {
      q = null
      continue
    }

    // 保证 e 与 φ(n) 互素，不满足则重新生成 p、q
    const p1 = p.subtract(ONE)
    const q1 = q.subtract(ONE)
    const phi = p1.multiply(q1)
    if (phi.gcd(e).compareTo(ONE) !== 0) {
      p = null
      q = null
      continue
    }

    // 保证 n 恰为 bits 位，不满足则重新生成 q
    const n = p.multiply(q)
    if (n.bitLength() !== bits) {
      q = null
      continue
    }

    const d = e.modInverse(phi)
    const privateKey = forge.pki.rsa.setPrivateKey(
      n,
      e,
      d,
      p,
      q,
      d.mod(p1),
      d.mod(q1),
      q.modInverse(p),
    )
    const publicKey = forge.pki.rsa.setPublicKey(n, e)

    // 导出公私钥为 PEM 格式
    return {
      publicKey: forge.pki.publicKeyToPem(publicKey),
      privateKey: forge.pki.privateKeyToPem(privateKey),
    }
  }
}

// 生成公私钥对
export const generateKeyPair = async () => {
  if (hasSubtleCrypto) {
    // 创建 RSA 密钥对（安全上下文下 node-forge 自动使用 Web Crypto 原生路径）
    const rsa = forge.pki.rsa
    const keyPair = await new Promise((resolve, reject) => {
      rsa.generateKeyPair({ bits: 2048, e: 0x10001 }, (err, keyPair) => {
        if (err) reject(err)
        resolve(keyPair)
      })
    })

    // 导出公私钥为 PEM 格式
    const publicKey = forge.pki.publicKeyToPem(keyPair.publicKey)
    const privateKey = forge.pki.privateKeyToPem(keyPair.privateKey)

    return {
      publicKey,
      privateKey,
    }
  }

  // 非安全上下文：使用快速纯 JS 实现
  return generateKeyPairByJsbn()
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

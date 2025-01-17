<script setup></script>

<template>
  <div class="about-page">
    <div class="title">
      <img
        class="logo"
        src="@/assets/MagicShare.png"
        height="488"
        width="522"
        alt="MagicShareLogo"
      />
      &nbsp;MagicShare<br />
      神奇分享
    </div>
    <div class="feat-container">
      <div class="feat-title">新特性：</div>
      <div class="feat-content">
        1. 自定义端口启动服务。&#10;<br />
        2. 按文件夹/文件路径查找文件并生成列表。&#10;<br />
        3. Web网页下载文件。&#10;&#9;<br />
        - 支持通过HTTP协议快速下载文件&#10;&#9;<br />
        - 支持通过WebSocket协议并使用RSA+AES混合加密下载文件
      </div>
    </div>
    <div class="content">Version：1.0.0<br />ZZHow.com</div>
  </div>
</template>

<style scoped lang="scss">
.title {
  display: flex;
  margin-top: 5vh;
  align-items: center;
  justify-content: center;
  text-align: center;
  font-size: 6vh;

  .logo {
    width: 20vw;
    height: auto;
  }
}

.feat-container {
  margin-top: 10vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  .feat-title {
    font-size: 2vh;
  }
  .feat-content {
    font-size: 2vh;
    margin: 0 2vh;
  }
}

.content {
  display: flex;
  margin-top: 5vh;
  align-items: center;
  justify-content: center;
  text-align: center;
  font-size: 2vh;
}
</style>

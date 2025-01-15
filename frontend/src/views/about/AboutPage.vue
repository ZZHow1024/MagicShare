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
      &#20; MagicShare<br />
      神奇分享
    </div>
    <div class="content">Version：1.0.0<br />Author: ZZHow</div>
  </div>
</template>

<style scoped lang="scss">
.logo {
  width: 28vw;
  height: auto;
}
.title {
  display: flex;
  margin-top: 5vh;
  align-items: center;
  justify-content: center;
  text-align: center;
  font-size: 8vw;
}
.content {
  display: flex;
  margin-top: 5vh;
  align-items: center;
  justify-content: center;
  text-align: center;
  font-size: 2vw;
}
</style>

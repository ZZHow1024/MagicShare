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
      &nbsp;
      <div>
        <div>MagicShare</div>
        <div v-if="$t('name') !== 'MagicShare'">{{ $t('name') }}</div>
      </div>
    </div>
    <div class="feat-container">
      <div class="feat-title">{{ $t('about.features') }}</div>
      <div class="feat-content">
        {{ $t('about.content') }}
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
    white-space: pre-wrap;
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

<template>
  <div class="auth-shell">
    <div class="auth-story">
      <NuxtLink class="brand" to="/login"
        ><span class="brand-mark"><AppIcon name="check" :size="25" /></span
        ><span>Resolve Log</span></NuxtLink
      >
      <div>
        <span class="eyebrow">YOUR DAILY RESOLUTION JOURNAL</span>
        <h1>결심은 작게.<br />기록은 꾸준하게.</h1>
        <p>
          오늘 해낸 것, 조금 아쉬웠던 것.<br />하루 한 줄로 나만의 변화를
          쌓아보세요.
        </p>
        <div class="paper-note">
          <span>Dear, tomorrow me</span>
          <p>오늘도 나와의 약속에<br />한 걸음 가까워졌어.</p>
          <div class="paper-check">
            <AppIcon name="check" />오늘의 작은 실천
          </div>
        </div>
      </div>
      <small>ONE DAY AT A TIME · RESOLVE LOG</small>
    </div>
    <div class="auth-form-area"><slot /></div>
  </div>
</template>

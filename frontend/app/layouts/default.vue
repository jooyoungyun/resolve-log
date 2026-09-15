<script setup lang="ts">
const auth = useAuth()
const route = useRoute()
</script>
<template>
  <div class="app-shell">
    <aside class="sidebar">
      <NuxtLink class="brand" to="/"
        ><span class="brand-mark"><AppIcon name="check" :size="25" /></span
        ><span>Resolve Log<small>작은 실천, 단단한 나</small></span></NuxtLink
      >
      <span class="nav-caption">MY JOURNEY</span>
      <nav aria-label="메인 메뉴">
        <NuxtLink to="/" :class="{ selected: route.path === '/' }"
          ><AppIcon name="home" />오늘의 일지</NuxtLink
        >
        <NuxtLink
          to="/goals"
          :class="{ selected: route.path.startsWith('/goals') }"
          ><AppIcon name="book" />나의 결심</NuxtLink
        >
        <NuxtLink to="/journal" :class="{ selected: route.path === '/journal' }"
          ><AppIcon name="calendar" />기록 보관함</NuxtLink
        >
      </nav>
      <div class="sidebar-note">
        <span class="tiny-label">ONE DAY AT A TIME</span>
        <p>완벽하지 않아도 괜찮아요.<br />오늘의 한 줄을 남겨보세요.</p>
        <span class="note-line"></span>
      </div>
      <div class="profile">
        <span class="avatar">{{
          auth.user.value?.displayName.slice(0, 1)
        }}</span>
        <div>
          <strong>{{ auth.user.value?.displayName }}</strong
          ><small>나의 결심을 기록하는 중</small>
        </div>
        <button
          class="icon-button"
          title="로그아웃"
          aria-label="로그아웃"
          @click="auth.logout"
        >
          <AppIcon name="logout" />
        </button>
      </div>
    </aside>
    <div class="workspace">
      <header class="topbar">
        <span>나를 위한 작은 기록장</span
        ><span class="topbar-tag"><i></i>조금씩, 꾸준히</span>
      </header>
      <main id="main-content"><slot /></main>
      <footer class="app-footer">
        Resolve Log <span>매일의 실천이 나의 이야기가 됩니다.</span>
      </footer>
    </div>
  </div>
</template>

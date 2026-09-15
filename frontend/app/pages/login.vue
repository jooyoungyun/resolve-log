<script setup lang="ts">
import type { AuthResult } from '~/types'
definePageMeta({ layout: 'auth' })
const route = useRoute()
const api = useApi()
const auth = useAuth()
const email = ref('')
const password = ref('')
const busy = ref(false)
const error = ref('')
async function submit() {
  if (busy.value) return
  busy.value = true
  error.value = ''
  try {
    auth.accept(
      await api<AuthResult>('/auth/login', {
        method: 'POST',
        body: { email: email.value, password: password.value },
      }),
    )
    await navigateTo('/')
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    busy.value = false
  }
}
</script>
<template>
  <section class="auth-panel">
    <span class="eyebrow">WELCOME BACK</span>
    <h1>다시, 나의 기록으로</h1>
    <p class="muted">오늘의 작은 실천을 기록해 볼까요?</p>
    <p v-if="route.query.expired" class="notice">
      로그인이 만료되었습니다. 다시 로그인해 주세요.
    </p>
    <form @submit.prevent="submit">
      <label
        >이메일<input
          v-model.trim="email"
          type="email"
          autocomplete="username"
          placeholder="you@example.com"
          required
          maxlength="254" /></label
      ><label
        >비밀번호<input
          v-model="password"
          type="password"
          autocomplete="current-password"
          placeholder="비밀번호를 입력해 주세요"
          required
          maxlength="64"
      /></label>
      <p v-if="error" class="error" role="alert">{{ error }}</p>
      <button class="button primary full" :disabled="busy">
        {{ busy ? '로그인 중…' : '로그인' }}<AppIcon name="arrow" />
      </button>
    </form>
    <p class="auth-switch">
      아직 기록장이 없으신가요? <NuxtLink to="/register">회원가입</NuxtLink>
    </p>
  </section>
</template>

<script setup lang="ts">
import type { AuthResult } from '~/types'
definePageMeta({ layout: 'auth' })
const api = useApi()
const auth = useAuth()
const form = reactive({ email: '', displayName: '', password: '' })
const confirm = ref('')
const busy = ref(false)
const error = ref('')
async function submit() {
  if (busy.value) return
  error.value = ''
  if (form.password !== confirm.value) {
    error.value = '비밀번호가 일치하지 않습니다.'
    return
  }
  if (new TextEncoder().encode(form.password).length > 72) {
    error.value = '비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.'
    return
  }
  busy.value = true
  try {
    auth.accept(
      await api<AuthResult>('/auth/register', { method: 'POST', body: form }),
    )
    await navigateTo('/goals/new')
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    busy.value = false
  }
}
</script>
<template>
  <section class="auth-panel">
    <span class="eyebrow">A FRESH START</span>
    <h1>나만의 기록장 만들기</h1>
    <p class="muted">작은 결심 하나로 시작해 보세요.</p>
    <form @submit.prevent="submit">
      <label
        >이름<input
          v-model.trim="form.displayName"
          autocomplete="nickname"
          placeholder="기록장에 사용할 이름"
          required
          minlength="2"
          maxlength="30" /></label
      ><label
        >이메일<input
          v-model.trim="form.email"
          type="email"
          autocomplete="username"
          placeholder="you@example.com"
          required
          maxlength="254" /></label
      ><label
        >비밀번호<input
          v-model="form.password"
          type="password"
          autocomplete="new-password"
          placeholder="8~64자"
          required
          minlength="8"
          maxlength="64" /></label
      ><label
        >비밀번호 확인<input
          v-model="confirm"
          type="password"
          autocomplete="new-password"
          placeholder="비밀번호를 한 번 더 입력해 주세요"
          required
      /></label>
      <p v-if="error" class="error" role="alert">{{ error }}</p>
      <button class="button primary full" :disabled="busy">
        {{ busy ? '만드는 중…' : '나의 첫 결심 시작하기'
        }}<AppIcon name="arrow" />
      </button>
    </form>
    <p class="auth-switch">
      이미 기록장이 있으신가요? <NuxtLink to="/login">로그인</NuxtLink>
    </p>
  </section>
</template>

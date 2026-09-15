<script setup lang="ts">
import type { Goal } from '~/types'
const route = useRoute()
const api = useApi()
const goal = ref<Goal | null>(null)
const error = ref('')
onMounted(async () => {
  try {
    goal.value = await api<Goal>('/goals/' + route.params.id)
  } catch (e) {
    error.value = errorMessage(e)
  }
})
</script>
<template>
  <div class="page-heading">
    <div>
      <NuxtLink class="back-link" to="/goals">← 나의 결심</NuxtLink>
      <h1>결심 다듬기</h1>
      <p>지속할 수 있도록, 나의 속도에 맞춰 조정해 보세요.</p>
    </div>
  </div>
  <p v-if="error" class="error" role="alert">{{ error }}</p>
  <GoalForm v-if="goal" :initial="goal" @saved="navigateTo('/goals')" />
  <div v-else-if="!error" class="loading" role="status">
    결심을 불러오는 중…
  </div>
</template>

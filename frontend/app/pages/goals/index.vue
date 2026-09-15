<script setup lang="ts">
import { categories, weekdayNames, type Goal, type Category } from '~/types'
const api = useApi()
const goals = ref<Goal[]>([])
const loading = ref(true)
const error = ref('')
const archived = ref(false)
const category = ref<Category | ''>('')
const shown = computed(() =>
  goals.value.filter(
    (g) =>
      g.archived === archived.value &&
      (!category.value || g.category === category.value),
  ),
)
onMounted(async () => {
  try {
    goals.value = await api<Goal[]>('/goals')
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
})
</script>
<template>
  <div class="page-heading">
    <div>
      <span class="eyebrow">MY RESOLUTIONS</span>
      <h1>나의 결심</h1>
      <p>지키고 싶은 작은 약속들을 모았어요.</p>
    </div>
    <NuxtLink class="button primary" to="/goals/new"
      ><AppIcon name="plus" />새로운 결심</NuxtLink
    >
  </div>
  <div class="filter-bar">
    <div class="tabs">
      <button :class="{ active: !archived }" @click="archived = false">
        실천 중
        <span>{{ goals.filter((g) => !g.archived).length }}</span></button
      ><button :class="{ active: archived }" @click="archived = true">
        보관한 결심
      </button>
    </div>
    <select v-model="category" aria-label="카테고리 필터">
      <option value="">모든 카테고리</option>
      <option v-for="(cat, key) in categories" :key="key" :value="key">
        {{ cat.label }}
      </option>
    </select>
  </div>
  <p v-if="error" class="error" role="alert">{{ error }}</p>
  <div v-else-if="loading" class="loading" role="status">
    결심을 불러오는 중…
  </div>
  <div v-else-if="shown.length" class="goals-grid">
    <article v-for="g in shown" :key="g.id" class="panel goal-tile">
      <div class="tile-top">
        <span class="category-icon" :class="categories[g.category].color"
          ><AppIcon :name="categories[g.category].icon" :size="23" /></span
        ><span class="badge">{{ categories[g.category].label }}</span>
      </div>
      <h2>{{ g.title }}</h2>
      <p class="goal-reason">
        {{ g.reason || '하루하루 실천하며 나만의 이유를 찾아가요.' }}
      </p>
      <div class="mini-weekdays">
        <span
          v-for="(day, i) in weekdayNames"
          :key="day"
          :class="{ on: g.weekdays.includes(i + 1) }"
          >{{ day }}</span
        >
      </div>
      <div class="tile-bottom">
        <small
          >{{ g.startDate }}부터{{
            g.endDate ? ' · ' + g.endDate + '까지' : ''
          }}</small
        ><NuxtLink :to="'/goals/' + g.id" class="text-button"
          ><AppIcon name="edit" :size="16" />수정</NuxtLink
        >
      </div>
    </article>
  </div>
  <div v-else class="empty-state panel">
    <span class="empty-icon"><AppIcon name="book" :size="32" /></span>
    <h2>
      {{ archived ? '보관한 결심이 없어요' : '작은 결심 하나로 시작해요' }}
    </h2>
    <p>
      {{
        archived
          ? '잠시 쉬어가는 결심도 이곳에서 간직할 수 있어요.'
          : '산책, 독서, 공부… 꾸준히 하고 싶은 일을 정해 보세요.'
      }}
    </p>
    <NuxtLink v-if="!archived" to="/goals/new" class="button primary"
      >첫 결심 만들기<AppIcon name="plus"
    /></NuxtLink>
  </div>
</template>

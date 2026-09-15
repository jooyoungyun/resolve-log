<script setup lang="ts">
import { categories, statuses, type Dashboard } from '~/types'
import { formatDay, todayInZone } from '~/utils/date'
const api = useApi()
const auth = useAuth()
const route = useRoute()
const config = useRuntimeConfig()
const initial =
  typeof route.query.date === 'string' &&
  /^\d{4}-\d{2}-\d{2}$/.test(route.query.date)
    ? route.query.date
    : todayInZone(config.public.timezone)
const date = ref(initial)
const data = ref<Dashboard | null>(null)
const loading = ref(false)
const error = ref('')
const message = ref('')
const opened = ref<string | null>(null)
const maxWeek = computed(() =>
  Math.max(1, ...(data.value?.week.map((d) => d.done) || [])),
)
let requestId = 0
async function load() {
  const id = ++requestId
  loading.value = true
  error.value = ''
  message.value = ''
  opened.value = null
  try {
    const result = await api<Dashboard>('/dashboard', {
      query: { date: date.value },
    })
    if (id !== requestId) return
    data.value = result
    const goal = String(route.query.goal || '')
    if (result.items.some((i) => i.goal.id === goal)) opened.value = goal
  } catch (e) {
    if (id === requestId) {
      error.value = errorMessage(e)
      data.value = null
    }
  } finally {
    if (id === requestId) loading.value = false
  }
}
async function saved() {
  await load()
  opened.value = null
  message.value = '오늘의 기록을 반영했어요.'
}
watch(
  date,
  () => {
    if (date.value) void load()
  },
  { immediate: true },
)
</script>
<template>
  <div class="page-heading">
    <div>
      <span class="eyebrow">MY DAILY JOURNAL</span>
      <h1>오늘도, 한 걸음.</h1>
      <p>
        {{ auth.user.value?.displayName }}님, 작은 실천 하나가 쌓여 변화를
        만들어요.
      </p>
    </div>
    <NuxtLink to="/goals/new" class="button primary"
      ><AppIcon name="plus" />새로운 결심</NuxtLink
    >
  </div>
  <div class="overview-grid">
    <section class="hero-panel">
      <div>
        <span class="tiny-label">A NOTE TO MYSELF</span>
        <h2>완벽한 하루보다<br /><em>기록하는 하루.</em></h2>
        <p>해낸 만큼, 느낀 만큼.<br />오늘의 나를 있는 그대로 남겨주세요.</p>
      </div>
      <div
        class="progress-ring"
        :style="{ '--progress': (data?.completionRate || 0) + '%' }"
      >
        <div>
          <strong>{{ data?.completionRate || 0 }}<span>%</span></strong
          ><small>선택한 날의 달성률</small>
        </div>
      </div>
    </section>
    <section class="panel week-panel">
      <span class="tiny-label">LAST 7 DAYS</span>
      <h3>꾸준함이 쌓이는 중</h3>
      <div
        class="week-chart"
        role="img"
        aria-label="선택한 날짜까지 최근 7일의 완료 기록 수"
      >
        <div
          v-for="day in data?.week || []"
          :key="day.date"
          class="week-column"
        >
          <span>{{ day.done }}</span>
          <div class="bar-track">
            <div
              class="bar"
              :class="{ today: day.date === date }"
              :style="{ height: Math.max(5, (day.done / maxWeek) * 100) + '%' }"
            ></div>
          </div>
          <small>{{ day.date.slice(8) }}일</small>
        </div>
      </div>
      <p class="help">하루에 완료한 결심의 수예요.</p>
    </section>
  </div>
  <div class="stats-row">
    <div>
      <span class="stat-icon green"><AppIcon name="book" /></span
      ><span
        >오늘의 결심<strong
          >{{ data?.targetCount || 0 }}<small>개</small></strong
        ></span
      >
    </div>
    <div>
      <span class="stat-icon orange"><AppIcon name="check" /></span
      ><span
        >해낸 결심<strong
          >{{ data?.doneCount || 0 }}<small>개</small></strong
        ></span
      >
    </div>
    <div>
      <span class="stat-icon blue"><AppIcon name="clock" /></span
      ><span
        >나를 위한 시간<strong
          >{{ data?.minutes || 0 }}<small>분</small></strong
        ></span
      >
    </div>
  </div>
  <div class="section-heading">
    <div>
      <h2>하루의 실천</h2>
      <p>{{ formatDay(date) }}의 이야기</p>
    </div>
    <label class="date-control"
      ><AppIcon name="calendar" /><input
        v-model="date"
        type="date"
        aria-label="기록 날짜"
        :max="data?.today || todayInZone(config.public.timezone)"
    /></label>
  </div>
  <p v-if="message" class="success" role="status">{{ message }}</p>
  <p v-if="error" class="error" role="alert">
    {{ error }} <button class="text-button" @click="load">다시 시도</button>
  </p>
  <div v-if="loading" class="loading" role="status">
    오늘의 결심을 불러오는 중…
  </div>
  <div v-else-if="data?.items.length" class="daily-list">
    <article
      v-for="item in data.items"
      :key="item.goal.id"
      class="panel daily-card"
      :class="{ completed: item.entry?.status === 'DONE' }"
    >
      <div class="daily-row">
        <span
          class="category-icon"
          :class="categories[item.goal.category].color"
          ><AppIcon :name="categories[item.goal.category].icon" :size="23"
        /></span>
        <div class="daily-title">
          <span class="tiny-label">{{
            categories[item.goal.category].label
          }}</span>
          <h3>{{ item.goal.title }}</h3>
          <p>
            {{
              item.entry?.note ||
              item.goal.reason ||
              '오늘의 작은 실천을 남겨주세요.'
            }}
          </p>
        </div>
        <span
          v-if="item.entry"
          class="status-badge"
          :class="item.entry.status.toLowerCase()"
          >{{ statuses[item.entry.status] }}</span
        ><button
          class="button"
          :class="item.entry ? 'secondary' : 'soft'"
          :disabled="date > data.today"
          @click="opened = opened === item.goal.id ? null : item.goal.id"
        >
          <AppIcon name="edit" :size="16" />{{
            item.entry ? '수정' : '기록하기'
          }}
        </button>
      </div>
      <EntryEditor
        v-if="opened === item.goal.id"
        :key="item.goal.id + date"
        :goal="item.goal"
        :date="date"
        :entry="item.entry"
        @saved="saved"
        @cancel="opened = null"
      />
    </article>
  </div>
  <div v-else-if="!error" class="empty-state panel">
    <span class="empty-icon"><AppIcon name="sun" :size="32" /></span>
    <h2>이 날짜에 예정된 결심이 없어요</h2>
    <p>새로운 결심을 만들거나, 다른 날짜의 기록을 살펴보세요.</p>
    <NuxtLink to="/goals/new" class="button primary"
      >작은 결심 시작하기<AppIcon name="arrow"
    /></NuxtLink>
  </div>
</template>

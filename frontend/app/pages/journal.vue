<script setup lang="ts">
import {
  categories,
  statuses,
  type CalendarResult,
  type Goal,
  type PageResult,
  type EntryStatus,
} from '~/types'
import { todayInZone, monthBounds } from '~/utils/date'
const api = useApi()
const config = useRuntimeConfig()
const today = todayInZone(config.public.timezone)
const month = ref(today.slice(0, 7))
const bounds = monthBounds(month.value)
const from = ref(bounds.from)
const to = ref(bounds.to)
const goalId = ref('')
const status = ref<EntryStatus | ''>('')
const page = ref(1)
const goals = ref<Goal[]>([])
const calendar = ref<CalendarResult | null>(null)
const records = ref<PageResult | null>(null)
const loading = ref(false)
const error = ref('')
const emptySlots = computed(
  () => (new Date(month.value + '-01T12:00:00').getDay() + 6) % 7,
)
const totalDone = computed(
  () => calendar.value?.days.reduce((n, d) => n + d.done, 0) || 0,
)
let requestId = 0
async function load() {
  if (!from.value || !to.value || !month.value) return
  const id = ++requestId
  loading.value = true
  error.value = ''
  try {
    const [cal, result] = await Promise.all([
      api<CalendarResult>('/calendar', { query: { month: month.value } }),
      api<PageResult>('/entries', {
        query: {
          from: from.value,
          to: to.value,
          page: page.value,
          goalId: goalId.value || undefined,
          status: status.value || undefined,
        },
      }),
    ])
    if (id !== requestId) return
    calendar.value = cal
    records.value = result
  } catch (e) {
    if (id === requestId) error.value = errorMessage(e)
  } finally {
    if (id === requestId) loading.value = false
  }
}
function search() {
  page.value = 1
  void load()
}
function selectDay(date: string) {
  from.value = date
  to.value = date
  search()
}
function showMonth() {
  if (!month.value) return
  const range = monthBounds(month.value)
  from.value = range.from
  to.value = range.to
  search()
}
watch(month, () => {
  if (month.value) {
    const b = monthBounds(month.value)
    from.value = b.from
    to.value = b.to
    search()
  }
})
onMounted(async () => {
  void load()
  try {
    goals.value = await api<Goal[]>('/goals')
  } catch (e) {
    error.value = errorMessage(e)
  }
})
function setPage(p: number) {
  page.value = p
  void load()
}
</script>
<template>
  <div class="page-heading">
    <div>
      <span class="eyebrow">COLLECTED MOMENTS</span>
      <h1>기록 보관함</h1>
      <p>하루하루 쌓아온 나의 이야기를 돌아보세요.</p>
    </div>
    <label class="date-control"
      ><AppIcon name="calendar" /><input
        v-model="month"
        type="month"
        aria-label="달력 월 선택"
    /></label>
  </div>
  <div class="journal-layout">
    <aside class="panel calendar-panel">
      <div class="calendar-title">
        <h2>{{ month.replace('-', '년 ') }}월</h2>
        <span class="badge">완료 {{ totalDone }}회</span>
      </div>
      <div class="calendar-grid">
        <span
          v-for="d in ['월', '화', '수', '목', '금', '토', '일']"
          :key="d"
          class="calendar-weekday"
          >{{ d }}</span
        ><span v-for="n in emptySlots" :key="'blank' + n"></span
        ><button
          v-for="d in calendar?.days || []"
          :key="d.date"
          :class="{
            recorded: d.done + d.partial + d.skipped > 0,
            done: d.done > 0,
            selected: from === d.date && to === d.date,
            current: d.date === today,
          }"
          :aria-label="
            d.date +
            ', 완료 ' +
            d.done +
            '개, 일부 수행 ' +
            d.partial +
            '개, 쉬어감 ' +
            d.skipped +
            '개'
          "
          @click="selectDay(d.date)"
        >
          {{ Number(d.date.slice(8))
          }}<i v-if="d.done + d.partial + d.skipped > 0"></i>
        </button>
      </div>
      <p class="help">
        날짜를 누르면 그날의 일지만 보여요.<br />달력은 모든 결심의 기록을
        표시합니다.
      </p>
      <button class="text-button" @click="showMonth">
        이번 달 전체 보기 →
      </button>
    </aside>
    <section class="journal-records">
      <form class="panel history-filter" @submit.prevent="search">
        <div class="form-row">
          <label>시작일<input v-model="from" type="date" required /></label
          ><label
            >종료일<input v-model="to" type="date" required :min="from"
          /></label>
        </div>
        <div class="filter-selects">
          <select v-model="goalId" aria-label="결심 필터">
            <option value="">모든 결심</option>
            <option v-for="g in goals" :key="g.id" :value="g.id">
              {{ g.title }}{{ g.archived ? ' (보관)' : '' }}
            </option></select
          ><select v-model="status" aria-label="상태 필터">
            <option value="">모든 상태</option>
            <option v-for="(text, key) in statuses" :key="key" :value="key">
              {{ text }}
            </option></select
          ><button class="button primary">조회</button>
        </div>
      </form>
      <div class="section-heading compact">
        <h2>
          나의 일지 <span class="count">{{ records?.totalElements || 0 }}</span>
        </h2>
        <span class="muted">최근 기록 순</span>
      </div>
      <p v-if="error" class="error" role="alert">{{ error }}</p>
      <div v-if="loading" class="loading" role="status">
        기록을 불러오는 중…
      </div>
      <div v-else-if="records?.items.length" class="timeline">
        <article
          v-for="entry in records.items"
          :key="entry.id"
          class="panel journal-entry"
        >
          <div class="entry-meta">
            <span>{{ entry.date }}</span
            ><span class="status-badge" :class="entry.status.toLowerCase()">{{
              statuses[entry.status]
            }}</span>
          </div>
          <h3>{{ entry.goalTitle }}</h3>
          <p class="journal-note">
            {{ entry.note || '이날은 실천 상태를 남겼어요.' }}
          </p>
          <div class="tile-bottom">
            <span class="muted"
              >{{ categories[entry.category].label }} · {{ entry.minutes }}분
              실천</span
            ><NuxtLink
              :to="{
                path: '/',
                query: { date: entry.date, goal: entry.goalId },
              }"
              class="text-button"
              >기록 수정<AppIcon name="arrow" :size="16"
            /></NuxtLink>
          </div>
        </article>
      </div>
      <div v-else-if="!error" class="empty-state panel">
        <AppIcon name="book" :size="32" />
        <h2>아직 남겨진 기록이 없어요</h2>
        <p>다른 기간을 살펴보거나 오늘의 일지를 남겨보세요.</p>
        <NuxtLink to="/" class="text-button">오늘의 일지로 →</NuxtLink>
      </div>
      <div v-if="records && records.totalPages > 1" class="pagination">
        <button
          class="button secondary"
          :disabled="page <= 1 || loading"
          @click="setPage(page - 1)"
        >
          이전</button
        ><span>{{ page }} / {{ records.totalPages }}</span
        ><button
          class="button secondary"
          :disabled="page >= records.totalPages || loading"
          @click="setPage(page + 1)"
        >
          다음
        </button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { categories, weekdayNames, type Goal, type GoalInput } from '~/types'
import { todayInZone } from '~/utils/date'
const props = defineProps<{ initial?: Goal }>()
const emit = defineEmits<{ saved: [goal: Goal] }>()
const api = useApi()
const config = useRuntimeConfig()
const form = reactive<GoalInput>(
  props.initial
    ? {
        title: props.initial.title,
        reason: props.initial.reason,
        category: props.initial.category,
        startDate: props.initial.startDate,
        endDate: props.initial.endDate,
        weekdays: [...props.initial.weekdays],
        archived: props.initial.archived,
        version: props.initial.version,
      }
    : {
        title: '',
        reason: '',
        category: 'HEALTH',
        startDate: todayInZone(config.public.timezone),
        endDate: null,
        weekdays: [1, 2, 3, 4, 5, 6, 7],
        archived: false,
      },
)
const busy = ref(false)
const error = ref('')
async function submit() {
  if (busy.value) return
  error.value = ''
  if (!form.weekdays.length) {
    error.value = '반복할 요일을 하나 이상 선택해 주세요.'
    return
  }
  if (form.endDate && form.endDate < form.startDate) {
    error.value = '종료일은 시작일 이후여야 합니다.'
    return
  }
  busy.value = true
  try {
    const result = await api<Goal>(
      props.initial ? '/goals/' + props.initial.id : '/goals',
      {
        method: props.initial ? 'PUT' : 'POST',
        body: {
          ...form,
          endDate: form.endDate || null,
          weekdays: [...form.weekdays].sort(),
        },
      },
    )
    emit('saved', result)
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    busy.value = false
  }
}
async function remove() {
  if (
    !props.initial ||
    busy.value ||
    !window.confirm(
      '이 결심과 모든 일지를 삭제할까요? 삭제한 기록은 복구할 수 없습니다.',
    )
  )
    return
  busy.value = true
  try {
    await api('/goals/' + props.initial.id, { method: 'DELETE' })
    await navigateTo('/goals')
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    busy.value = false
  }
}
</script>
<template>
  <form class="panel goal-form" @submit.prevent="submit">
    <div class="form-section">
      <span class="step-dot">01</span>
      <div class="form-section-body">
        <h2>어떤 결심을 해볼까요?</h2>
        <label
          >결심 이름 <span class="required">*</span
          ><input
            v-model.trim="form.title"
            placeholder="예: 하루 30분, 나를 위한 산책"
            required
            maxlength="100" /></label
        ><label
          >이 결심을 시작하는 이유<textarea
            v-model="form.reason"
            rows="3"
            maxlength="1000"
            placeholder="미래의 내가 잊지 않았으면 하는 마음을 적어주세요."
          ></textarea>
        </label>
      </div>
    </div>
    <div class="form-section">
      <span class="step-dot">02</span>
      <div class="form-section-body">
        <h2>어느 영역의 결심인가요?</h2>
        <div class="category-picker">
          <label
            v-for="(cat, key) in categories"
            :key="key"
            :class="{ chosen: form.category === key }"
            ><input
              v-model="form.category"
              type="radio"
              name="category"
              :value="key"
            /><AppIcon :name="cat.icon" />{{ cat.label }}</label
          >
        </div>
      </div>
    </div>
    <div class="form-section">
      <span class="step-dot">03</span>
      <div class="form-section-body">
        <h2>나에게 맞는 리듬 정하기</h2>
        <div class="form-row">
          <label
            >시작일<input
              v-model="form.startDate"
              type="date"
              required /></label
          ><label
            >종료일 <span class="muted">(선택)</span
            ><input v-model="form.endDate" type="date" :min="form.startDate"
          /></label>
        </div>
        <label>반복 요일 <span class="required">*</span></label>
        <div class="weekday-picker">
          <label
            v-for="(day, i) in weekdayNames"
            :key="day"
            :class="{ chosen: form.weekdays.includes(i + 1) }"
            ><input v-model="form.weekdays" type="checkbox" :value="i + 1" />{{
              day
            }}</label
          >
        </div>
        <p class="help">
          선택한 요일에 결심이 나타납니다. 지나간 날짜에도 일지를 기록할 수
          있어요.
        </p>
        <label v-if="initial" class="checkbox-label"
          ><input v-model="form.archived" type="checkbox" />이 결심 보관하기
          <span class="muted"
            >기존 일지는 보관함에서 계속 볼 수 있어요.</span
          ></label
        >
      </div>
    </div>
    <p v-if="error" class="error" role="alert">{{ error }}</p>
    <div class="form-actions">
      <button
        v-if="initial"
        type="button"
        class="text-button danger"
        :disabled="busy"
        @click="remove"
      >
        결심 삭제</button
      ><NuxtLink class="button secondary" to="/goals">취소</NuxtLink
      ><button class="button primary" :disabled="busy">
        {{
          busy ? '저장 중…' : initial ? '변경 내용 저장' : '나의 결심 시작하기'
        }}<AppIcon name="arrow" />
      </button>
    </div>
  </form>
</template>

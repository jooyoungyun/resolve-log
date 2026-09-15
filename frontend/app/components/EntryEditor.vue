<script setup lang="ts">
import { statuses, type Goal, type Entry, type EntryStatus } from '~/types'
const props = defineProps<{ goal: Goal; date: string; entry: Entry | null }>()
const emit = defineEmits<{ saved: []; cancel: [] }>()
const api = useApi()
const form = reactive({
  status: props.entry?.status || ('DONE' as EntryStatus),
  note: props.entry?.note || '',
  minutes: props.entry?.minutes || 0,
})
const busy = ref(false)
const error = ref('')
async function save() {
  if (busy.value) return
  busy.value = true
  error.value = ''
  try {
    await api('/goals/' + props.goal.id + '/entries/' + props.date, {
      method: 'PUT',
      body: form,
    })
    emit('saved')
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    busy.value = false
  }
}
async function remove() {
  if (busy.value || !window.confirm('이 날짜의 일지를 삭제할까요?')) return
  busy.value = true
  try {
    await api('/goals/' + props.goal.id + '/entries/' + props.date, {
      method: 'DELETE',
    })
    emit('saved')
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    busy.value = false
  }
}
</script>
<template>
  <form class="entry-editor" @submit.prevent="save">
    <div class="status-picker">
      <label
        v-for="(label, status) in statuses"
        :key="status"
        :class="[{ chosen: form.status === status }, status.toLowerCase()]"
        ><input
          v-model="form.status"
          type="radio"
          :name="'status-' + goal.id"
          :value="status"
        />{{ status === 'DONE' ? '✓' : status === 'PARTIAL' ? '◐' : '–' }}
        {{ label }}</label
      >
    </div>
    <label
      >오늘의 기록<textarea
        v-model="form.note"
        rows="4"
        maxlength="4000"
        placeholder="어떤 실천을 했나요? 오늘의 생각과 내일의 나에게 하고 싶은 말을 남겨보세요."
      ></textarea>
    </label>
    <div class="editor-bottom">
      <label class="minutes-field"
        >실천 시간<input
          v-model.number="form.minutes"
          type="number"
          min="0"
          max="1440"
          step="1"
          required
        /><span>분</span></label
      ><small class="muted">{{ form.note.length }} / 4,000</small>
    </div>
    <p v-if="error" class="error" role="alert">{{ error }}</p>
    <div class="editor-actions">
      <button
        v-if="entry"
        type="button"
        class="text-button danger"
        :disabled="busy"
        @click="remove"
      >
        기록 삭제</button
      ><button
        type="button"
        class="button secondary"
        :disabled="busy"
        @click="emit('cancel')"
      >
        닫기</button
      ><button class="button primary" :disabled="busy">
        {{ busy ? '저장 중…' : '일지 저장' }}<AppIcon name="check" :size="16" />
      </button>
    </div>
  </form>
</template>

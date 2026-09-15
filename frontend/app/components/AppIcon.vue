<script setup lang="ts">
defineProps<{ name?: string; size?: number }>()
const paths: Record<string, string> = {
  check: 'M5 12l4 4L19 6',
  plus: 'M12 5v14M5 12h14',
  home: 'M3 10l9-7 9 7v10a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1zM9 21v-8h6v8',
  book: 'M4 3h12a3 3 0 0 1 3 3v15H7a3 3 0 0 1-3-3zm0 14h15M8 7h7M8 11h5',
  calendar: 'M5 5h14v16H5zM8 3v4M16 3v4M5 10h14',
  heart: 'M20 5c-3-3-7-1-8 2-1-3-5-5-8-2-4 4 1 9 8 15 7-6 12-11 8-15z',
  work: 'M3 7h18v13H3zM8 7V4h8v3M3 12c6 4 12 4 18 0',
  sun: 'M12 2v2m0 16v2M2 12h2m16 0h2M5 5l2 2m10 10 2 2M5 19l2-2M17 7l2-2M16 12a4 4 0 1 1-8 0 4 4 0 0 1 8 0',
  chart: 'M4 20V4m0 16h17M8 16v-4m5 4V8m5 8V5',
  star: 'M12 3l3 6 6 1-4 5 1 6-6-3-6 3 1-6-4-5 6-1z',
  arrow: 'M5 12h14m-5-5 5 5-5 5',
  logout: 'M9 4H4v16h5m3-8h9m-4-4 4 4-4 4',
  edit: 'M14 5l5 5M4 20l5-1L21 7l-4-4L5 15z',
  clock: 'M12 8v5l3 2M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0',
}
</script>
<template>
  <svg
    :width="size || 20"
    :height="size || 20"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.7"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <path :d="paths[name || 'check'] || paths.check" />
  </svg>
</template>

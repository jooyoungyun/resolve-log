export function todayInZone(timezone = 'Asia/Seoul'): string {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: timezone,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(new Date())
  const get = (type: string) => parts.find((p) => p.type === type)?.value ?? ''
  return [get('year'), get('month'), get('day')].join('-')
}
export function formatDay(date: string): string {
  if (Number.isNaN(new Date(date + 'T12:00:00').getTime())) return date
  return new Intl.DateTimeFormat('ko-KR', {
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  }).format(new Date(date + 'T12:00:00'))
}
export function monthBounds(month: string): { from: string; to: string } {
  const [year, m] = month.split('-').map(Number)
  const last = new Date(year!, m!, 0).getDate()
  return { from: month + '-01', to: month + '-' + last }
}

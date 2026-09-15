export type Category =
  | 'HEALTH'
  | 'STUDY'
  | 'WORK'
  | 'LIFE'
  | 'FINANCE'
  | 'OTHER'
export type EntryStatus = 'DONE' | 'PARTIAL' | 'SKIPPED'
export interface User {
  id: string
  email: string
  displayName: string
}
export interface AuthResult {
  accessToken: string
  user: User
}
export interface Goal {
  id: string
  title: string
  reason: string
  category: Category
  startDate: string
  endDate: string | null
  weekdays: number[]
  archived: boolean
  version: number
}
export type GoalInput = Omit<Goal, 'id' | 'version'> & { version?: number }
export interface Entry {
  id: string
  goalId: string
  goalTitle: string
  category: Category
  date: string
  status: EntryStatus
  note: string
  minutes: number
  updatedAt: string
}
export interface DayCount {
  date: string
  done: number
  partial: number
  skipped: number
  minutes: number
}
export interface Dashboard {
  date: string
  today: string
  items: { goal: Goal; entry: Entry | null }[]
  doneCount: number
  targetCount: number
  minutes: number
  completionRate: number
  week: DayCount[]
}
export interface PageResult {
  items: Entry[]
  currentPage: number
  totalPages: number
  totalElements: number
}
export interface CalendarResult {
  month: string
  days: DayCount[]
}
export const categories: Record<
  Category,
  { label: string; color: string; icon: string }
> = {
  HEALTH: { label: '건강·운동', color: 'green', icon: 'heart' },
  STUDY: { label: '공부·성장', color: 'blue', icon: 'book' },
  WORK: { label: '일·커리어', color: 'orange', icon: 'work' },
  LIFE: { label: '일상·마음', color: 'purple', icon: 'sun' },
  FINANCE: { label: '재무·투자', color: 'teal', icon: 'chart' },
  OTHER: { label: '그 밖의 결심', color: 'gray', icon: 'star' },
}
export const statuses: Record<EntryStatus, string> = {
  DONE: '해냈어요',
  PARTIAL: '조금 했어요',
  SKIPPED: '쉬어갔어요',
}
export const weekdayNames = ['월', '화', '수', '목', '금', '토', '일']

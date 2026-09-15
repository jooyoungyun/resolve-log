type ApiOptions = {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: object
  query?: Record<string, string | number | undefined>
}
export function errorMessage(error: unknown): string {
  const e = error as {
    data?: { message?: string; fieldErrors?: Record<string, string> }
    message?: string
  }
  const fields = e.data?.fieldErrors
    ? Object.values(e.data.fieldErrors).join(' · ')
    : ''
  return (
    fields ||
    e.data?.message ||
    '요청을 처리하지 못했습니다. 서버 연결을 확인해 주세요.'
  )
}
export function useApi() {
  const auth = useAuth()
  return async function request<T>(
    url: string,
    options: ApiOptions = {},
  ): Promise<T> {
    try {
      return (await $fetch<T>('/api' + url, {
        ...options,
        retry: 0,
        timeout: 15000,
        headers: auth.token.value
          ? { Authorization: 'Bearer ' + auth.token.value }
          : {},
      })) as T
    } catch (error) {
      const e = error as { response?: { status?: number } }
      if (e.response?.status === 401 && !url.startsWith('/auth/')) {
        auth.clear()
        await navigateTo('/login?expired=1')
      }
      throw error
    }
  }
}

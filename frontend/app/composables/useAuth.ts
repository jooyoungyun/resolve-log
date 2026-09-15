import type { AuthResult, User } from '~/types'

export function useAuth() {
  const token = useState<string>('auth-token', () => '')
  const user = useState<User | null>('auth-user', () => null)
  const initialized = useState<boolean>('auth-initialized', () => false)
  function initialize() {
    if (!import.meta.client || initialized.value) return
    initialized.value = true
    try {
      const stored = JSON.parse(
        localStorage.getItem('resolve-log-auth') || 'null',
      ) as AuthResult | null
      if (stored?.accessToken && stored.user) {
        token.value = stored.accessToken
        user.value = stored.user
      }
    } catch {
      localStorage.removeItem('resolve-log-auth')
    }
  }
  function accept(result: AuthResult) {
    token.value = result.accessToken
    user.value = result.user
    localStorage.setItem('resolve-log-auth', JSON.stringify(result))
  }
  function clear() {
    token.value = ''
    user.value = null
    if (import.meta.client) localStorage.removeItem('resolve-log-auth')
  }
  async function logout() {
    clear()
    await navigateTo('/login')
  }
  initialize()
  return { token, user, accept, clear, logout }
}

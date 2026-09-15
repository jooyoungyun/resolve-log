export default defineNuxtRouteMiddleware((to) => {
  if (import.meta.server) return
  const { token } = useAuth()
  const isPublic = ['/login', '/register'].includes(to.path)
  if (!token.value && !isPublic) return navigateTo('/login')
  if (token.value && isPublic) return navigateTo('/')
})

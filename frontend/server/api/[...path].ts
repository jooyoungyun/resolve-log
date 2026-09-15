export default defineEventHandler((event) => {
  const config = useRuntimeConfig(event)
  const url = getRequestURL(event)
  // The upstream host is server configuration only, never a user-supplied URL.
  return proxyRequest(
    event,
    config.apiBase.replace(/\/$/, '') + url.pathname + url.search,
  )
})

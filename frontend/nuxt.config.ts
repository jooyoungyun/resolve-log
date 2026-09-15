export default defineNuxtConfig({
  compatibilityDate: '2026-09-15',
  ssr: false,
  devtools: { enabled: false },
  css: ['~/assets/css/main.css'],
  typescript: { strict: true },
  runtimeConfig: {
    apiBase: 'http://127.0.0.1:8080',
    public: { appName: 'Resolve Log', timezone: 'Asia/Seoul' },
  },
  app: {
    head: {
      title: 'Resolve Log · 매일 쌓이는 나의 결심',
      htmlAttrs: { lang: 'ko' },
      meta: [
        {
          name: 'description',
          content: '결심을 정하고, 하루의 실천과 생각을 기록하세요.',
        },
      ],
    },
  },
})

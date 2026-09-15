import { expect, test } from '@playwright/test'

test('register, create a goal, write and revise a journal, then delete the goal', async ({
  page,
}) => {
  const errors: string[] = []
  page.on('pageerror', (error) => errors.push(error.message))
  await page.goto('/register')
  await page.getByLabel('이름', { exact: true }).fill('기록 테스트')
  await page
    .getByLabel('이메일', { exact: true })
    .fill(`journal-${Date.now()}@example.com`)
  await page.getByLabel('비밀번호', { exact: true }).fill('ResolveTest2026!')
  await page
    .getByLabel('비밀번호 확인', { exact: true })
    .fill('ResolveTest2026!')
  await page.getByRole('button', { name: '나의 첫 결심 시작하기' }).click()
  await expect(page).toHaveURL(/\/goals\/new$/)
  await page.getByLabel('결심 이름').fill('하루 30분, 나를 위한 산책')
  await page
    .getByLabel('이 결심을 시작하는 이유')
    .fill('일상 속에서 나를 돌보는 시간을 만들어요.')
  await page.getByRole('button', { name: '나의 결심 시작하기' }).click()
  await expect(
    page.getByRole('heading', { name: '하루 30분, 나를 위한 산책' }),
  ).toBeVisible()
  await page.getByRole('button', { name: '기록하기', exact: true }).click()
  await page
    .getByLabel('오늘의 기록')
    .fill('저녁 공원을 걸었다. 하루를 가볍게 마무리했다.')
  await page.getByLabel('실천 시간').fill('30')
  await page.getByRole('button', { name: '일지 저장' }).click()
  await expect(page.getByRole('status')).toHaveText('오늘의 기록을 반영했어요.')
  await expect(page.locator('.status-badge')).toHaveText('해냈어요')
  await page.getByRole('link', { name: '기록 보관함', exact: true }).click()
  await expect(page.locator('.journal-entry')).toHaveCount(1)
  await expect(page.locator('.journal-note')).toContainText(
    '저녁 공원을 걸었다.',
  )
  await page.getByRole('link', { name: '기록 수정' }).click()
  await page.locator('.status-picker label').filter({ hasText: '조금 했어요' }).click()
  await expect(page.getByRole('radio', { name: /조금 했어요/ })).toBeChecked()
  await page
    .getByLabel('오늘의 기록')
    .fill('비가 와서 짧게 걸었지만, 나와의 약속을 기억했다.')
  await page.getByLabel('실천 시간').fill('15')
  await page.getByRole('button', { name: '일지 저장' }).click()
  await expect(page.getByRole('status')).toHaveText('오늘의 기록을 반영했어요.')
  await expect(page.locator('.status-badge')).toHaveText('조금 했어요')
  await page.reload()
  await expect(page.locator('.daily-title')).toContainText(
    '비가 와서 짧게 걸었지만',
  )
  await page.setViewportSize({ width: 390, height: 844 })
  await expect(
    page.getByRole('heading', { name: '오늘도, 한 걸음.' }),
  ).toBeVisible()
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth,
    ),
  ).toBe(true)
  await page.getByRole('link', { name: '나의 결심', exact: true }).click()
  await page.getByRole('link', { name: '수정', exact: true }).click()
  page.once('dialog', (dialog) => dialog.accept())
  await page.getByRole('button', { name: '결심 삭제', exact: true }).click()
  await expect(page).toHaveURL(/\/goals$/)
  await page.getByRole('link', { name: '기록 보관함', exact: true }).click()
  await expect(
    page.getByRole('heading', { name: '아직 남겨진 기록이 없어요' }),
  ).toBeVisible()
  await page.getByRole('button', { name: '로그아웃', exact: true }).click()
  await expect(page).toHaveURL(/\/login$/)
  expect(errors).toEqual([])
})

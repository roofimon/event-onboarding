import { expect, type Page, test } from '@playwright/test'

test('completes the onboarding flow to approval', async ({ page }) => {
  await mockOnboardingApi(page, { score: 80, approved: true, finalStep: 'COMPLETED' })

  await page.goto('/')
  await page.getByLabel('Email address').fill('ada@example.com')
  await page.getByRole('button', { name: 'Continue' }).click()

  await expect(page.getByRole('heading', { name: /verify token/i })).toBeVisible()
  await page.getByLabel('Verification token').fill('123456')
  await page.getByRole('button', { name: 'Verify' }).click()

  await expect(page.getByRole('heading', { name: /your details/i })).toBeVisible()
  await page.getByLabel('Full name').fill('Ada Lovelace')
  await page.getByLabel('Email address').fill('ada@example.com')
  await page.getByLabel('Phone number').fill('+1 555 0100')
  await page.getByRole('button', { name: 'Submit & get result' }).click()

  await expect(page).toHaveURL(/\/welcome$/)
  await expect(page.getByRole('heading', { name: /welcome aboard, ada lovelace/i })).toBeVisible()
  await expect(page.locator('.score')).toHaveText('80')
})

test('completes the onboarding flow to decline', async ({ page }) => {
  await mockOnboardingApi(page, { score: 40, approved: false, finalStep: 'DECLINED' })

  await page.goto('/')
  await page.getByLabel('Email address').fill('grace@example.com')
  await page.getByRole('button', { name: 'Continue' }).click()

  await page.getByLabel('Verification token').fill('123456')
  await page.getByRole('button', { name: 'Verify' }).click()

  await page.getByLabel('Full name').fill('Grace Hopper')
  await page.getByLabel('Email address').fill('grace@example.com')
  await page.getByLabel('Phone number').fill('+1 555 0101')
  await page.getByRole('button', { name: 'Submit & get result' }).click()

  await expect(page).toHaveURL(/\/declined$/)
  await expect(page.getByRole('heading', { name: /application not approved/i })).toBeVisible()
  await expect(page.locator('.score')).toHaveText('40')
})

test('shows an API validation error on the first step', async ({ page }) => {
  await page.route('**/api/onboarding/start', async (route) => {
    await route.fulfill({
      status: 400,
      contentType: 'application/json',
      body: JSON.stringify({ error: 'email: must be a well-formed email address' }),
    })
  })

  await page.goto('/')
  await page.getByLabel('Email address').fill('broken@example.com')
  await page.getByRole('button', { name: 'Continue' }).click()

  await expect(page.getByText('email: must be a well-formed email address')).toBeVisible()
  await expect(page).toHaveURL(/\/$/)
})

async function mockOnboardingApi(
  page: Page,
  result: { score: number; approved: boolean; finalStep: 'COMPLETED' | 'DECLINED' },
) {
  await page.route('**/api/onboarding/start', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ applicationId: 'app-123', step: 'TOKEN_VERIFY' }),
    })
  })

  await page.route('**/api/onboarding/app-123/verify-token', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ verified: true, step: 'FULFILLMENT' }),
    })
  })

  await page.route('**/api/onboarding/app-123/fulfillment', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ step: 'SCORING' }),
    })
  })

  await page.route('**/api/onboarding/app-123/score', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        score: result.score,
        approved: result.approved,
        step: result.finalStep,
      }),
    })
  })
}

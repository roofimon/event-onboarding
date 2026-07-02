import { expect, type APIRequestContext, test } from '@playwright/test'

test('completes the onboarding flow to approval', async ({ page, request }) => {
  await configureScenario(request)

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
  // 120k over 7 years scores 55 via the weighted algorithm -> above the 40 threshold.
  await page.getByLabel('Salary').fill('120000')
  await expect(page.getByLabel('Salary')).toHaveValue('120,000')
  await page.getByLabel('Years of experience').fill('7')
  await page.getByRole('button', { name: 'Submit & get result' }).click()

  await expect(page).toHaveURL(/\/welcome$/)
  await expect(page.getByRole('heading', { name: /welcome aboard, ada lovelace/i })).toBeVisible()
  await expect(page.locator('.score')).toHaveText('55')

  // Log in with the credentials issued on approval and view the profile.
  await page.getByRole('button', { name: 'Log in to your account' }).click()
  await expect(page).toHaveURL(/\/login$/)
  await page.getByLabel('Email address').fill('ada@example.com')
  await page.getByLabel('Password').fill('e2e-password-123')
  await page.getByRole('button', { name: 'Log in' }).click()

  await expect(page).toHaveURL(/\/profile$/)
  await expect(page.getByRole('heading', { name: /your profile/i })).toBeVisible()
  const profile = page.locator('.profile')
  await expect(profile).toContainText('Ada Lovelace')
  await expect(profile).toContainText('ada@example.com')
  await expect(profile).toContainText('+1 555 0100')
  await expect(profile).toContainText('120,000')
  await expect(profile).toContainText('7')
})

test('rejects login with a wrong password', async ({ page, request }) => {
  await configureScenario(request)

  await page.goto('/login')
  await page.getByLabel('Email address').fill('ada@example.com')
  await page.getByLabel('Password').fill('wrong-password')
  await page.getByRole('button', { name: 'Log in' }).click()

  await expect(page.getByText(/invalid email or password/i)).toBeVisible()
  await expect(page).toHaveURL(/\/login$/)
})

test('completes the onboarding flow to decline', async ({ page, request }) => {
  await configureScenario(request)

  await page.goto('/')
  await page.getByLabel('Email address').fill('grace@example.com')
  await page.getByRole('button', { name: 'Continue' }).click()

  await page.getByLabel('Verification token').fill('123456')
  await page.getByRole('button', { name: 'Verify' }).click()

  await page.getByLabel('Full name').fill('Grace Hopper')
  await page.getByLabel('Email address').fill('grace@example.com')
  await page.getByLabel('Phone number').fill('+1 555 0101')
  // 35k over 2 years scores 22 via the weighted algorithm -> at or below the 40 threshold.
  await page.getByLabel('Salary').fill('35000')
  await page.getByLabel('Years of experience').fill('2')
  await page.getByRole('button', { name: 'Submit & get result' }).click()

  await expect(page).toHaveURL(/\/declined$/)
  await expect(page.getByRole('heading', { name: /application not approved/i })).toBeVisible()
  await expect(page.locator('.score')).toHaveText('22')
})

test('shows a backend error when token verification fails', async ({ page, request }) => {
  await configureScenario(request)

  await page.goto('/')
  await page.getByLabel('Email address').fill('wrong-token@example.com')
  await page.getByRole('button', { name: 'Continue' }).click()

  await page.getByLabel('Verification token').fill('000000')
  await page.getByRole('button', { name: 'Verify' }).click()

  await expect(page.getByText('Invalid verification token')).toBeVisible()
  await expect(page).toHaveURL(/\/verify$/)
})

async function configureScenario(request: APIRequestContext) {
  const response = await request.post('http://127.0.0.1:18080/api/e2e/scenario', {
    data: { token: '123456' },
  })
  expect(response.ok()).toBeTruthy()
}

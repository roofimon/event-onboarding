import { expect, type APIRequestContext, type Page, test } from "@playwright/test";

test("completes an approved onboarding flow and updates the profile", async ({ page, request }) => {
  await configureScenario(request);

  await completeApprovedOnboarding(page, "ada@example.com", "Ada Lovelace");

  await expect(page).toHaveURL(/\/welcome$/);
  await expect(page.getByRole("heading", { name: /welcome aboard, ada lovelace/i })).toBeVisible();
  await expect(page.locator(".score")).toHaveText("55");

  await page.getByRole("button", { name: "Log in to your account" }).click();
  await page.getByLabel("Email address").fill("ada@example.com");
  await page.getByLabel("Password").fill("e2e-password-123");
  await page.getByRole("button", { name: "Log in" }).click();

  await expect(page).toHaveURL(/\/profile$/);
  const profile = page.locator(".profile");
  await expect(profile).toContainText("Ada Lovelace");
  await expect(profile).toContainText("+1 555 0100");
  await expect(profile).toContainText("120,000");
  await expect(profile).toContainText("7 years");

  await page.getByRole("button", { name: "Edit profile" }).click();
  await page.getByLabel("Phone number").fill("+1 555 9999");
  await page.getByLabel("Annual salary").fill("150000");
  await page.getByLabel("Confirm password").fill("e2e-password-123");
  await page.getByRole("button", { name: "Save changes" }).click();

  await expect(profile).toContainText("+1 555 9999");
  await expect(profile).toContainText("150,000");
});

test("rejects a profile edit with a wrong password", async ({ page, request }) => {
  await configureScenario(request);

  await completeApprovedOnboarding(page, "edith@example.com", "Edith Clarke");
  await page.getByRole("button", { name: "Log in to your account" }).click();
  await page.getByLabel("Email address").fill("edith@example.com");
  await page.getByLabel("Password").fill("e2e-password-123");
  await page.getByRole("button", { name: "Log in" }).click();

  await page.getByRole("button", { name: "Edit profile" }).click();
  await page.getByLabel("Phone number").fill("+1 555 1234");
  await page.getByLabel("Confirm password").fill("wrong-password");
  await page.getByRole("button", { name: "Save changes" }).click();

  await expect(page.getByText(/invalid email or password/i)).toBeVisible();
});

test("rejects login with a wrong password", async ({ page, request }) => {
  await configureScenario(request);

  await page.goto("/login");
  await page.getByLabel("Email address").fill("ada@example.com");
  await page.getByLabel("Password").fill("wrong-password");
  await page.getByRole("button", { name: "Log in" }).click();

  await expect(page.getByText(/invalid email or password/i)).toBeVisible();
  await expect(page).toHaveURL(/\/login$/);
});

test("completes a declined onboarding flow", async ({ page, request }) => {
  await configureScenario(request);

  await page.goto("/");
  await page.getByLabel("Email address").fill("grace@example.com");
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByLabel("Verification token").fill("123456");
  await page.getByRole("button", { name: /verify & continue/i }).click();
  await page.getByLabel("Full name").fill("Grace Hopper");
  await page.getByLabel("Email address").fill("grace@example.com");
  await page.getByLabel("Phone number").fill("+1 555 0101");
  await page.getByLabel("Annual salary").fill("35000");
  await page.getByLabel("Experience").fill("2");
  await page.getByRole("button", { name: "Submit application" }).click();

  await expect(page).toHaveURL(/\/declined$/);
  await expect(page.getByRole("heading", { name: /application not approved/i })).toBeVisible();
  await expect(page.locator(".score")).toHaveText("22");
});

test("shows a backend error for an invalid verification token", async ({ page, request }) => {
  await configureScenario(request);

  await page.goto("/");
  await page.getByLabel("Email address").fill("wrong-token@example.com");
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByLabel("Verification token").fill("000000");
  await page.getByRole("button", { name: /verify & continue/i }).click();

  await expect(page.getByText("Invalid verification token")).toBeVisible();
  await expect(page).toHaveURL(/\/verify$/);
});

async function completeApprovedOnboarding(
  page: Page,
  email: string,
  name: string,
) {
  await page.goto("/");
  await page.getByLabel("Email address").fill(email);
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByLabel("Verification token").fill("123456");
  await page.getByRole("button", { name: /verify & continue/i }).click();
  await page.getByLabel("Full name").fill(name);
  await page.getByLabel("Email address").fill(email);
  await page.getByLabel("Phone number").fill("+1 555 0100");
  await page.getByLabel("Annual salary").fill("120000");
  await expect(page.getByLabel("Annual salary")).toHaveValue("120,000");
  await page.getByLabel("Experience").fill("7");
  await page.getByRole("button", { name: "Submit application" }).click();
  await expect(page).toHaveURL(/\/welcome$/);
}

async function configureScenario(request: APIRequestContext) {
  const response = await request.post("http://127.0.0.1:18080/api/e2e/scenario", {
    data: { token: "123456" },
  });

  expect(response.ok()).toBeTruthy();
}

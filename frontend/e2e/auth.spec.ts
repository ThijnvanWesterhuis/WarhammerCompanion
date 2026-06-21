import { expect, test } from '@playwright/test';
import { uniqueUser } from './helpers/auth';

test('guest is redirected to login when opening a protected page', async ({ page }) => {
  await page.goto('/session');

  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole('heading', { name: 'Authenticate' })).toBeVisible();
  await expect(page.getByLabel('Operator ID')).toBeVisible();
  await expect(page.getByLabel('Security Cipher')).toBeVisible();
});

test('user can register and lands on the profile page', async ({ page }) => {
  const user = uniqueUser('register');

  await page.goto('/register');

  await page.getByLabel('Operator ID').fill(user.username);
  await page.getByLabel('Email Routing').fill(user.email);
  await page.getByLabel('Security Cipher').fill(user.password);
  await page.getByLabel('Confirm Cipher').fill(user.confirmPassword);
  await page.getByRole('button', { name: 'Create Account' }).click();

  await expect(page).toHaveURL(/\/profile$/);
  await expect(page.getByRole('heading', { name: user.username })).toBeVisible();
  await expect(page.getByText(user.email, { exact: true })).toBeVisible();
  await expect(page.getByText('JWT Token Guard', { exact: true })).toBeVisible();
});

import { expect, test } from '@playwright/test';
import { registerAndLoginByApi } from './helpers/auth';

test('user can start a session, update score, save it and see it in match history', async ({ page, request }) => {
  await registerAndLoginByApi(page, request, undefined);

  await page.goto('/session');

  await page.locator('#playerOneName').fill('Tester One');
  await page.locator('#playerTwoName').fill('Tester Two');
  await page.locator('#playerOneFaction').selectOption('Space Marines');
  await page.locator('#playerTwoFaction').selectOption('Necrons');
  await page.locator('#missionName').fill('Take and Hold');
  await page.locator('#deploymentMap').fill('Dawn of War');
  await page.locator('#notes').fill('Created by Playwright E2E test');

  await page.getByRole('button', { name: 'Start New Mission' }).click();

  await expect(page.getByText('Session started.')).toBeVisible();
  await expect(page.getByText('01/05')).toBeVisible();
  await expect(page.getByText('Tester One')).toBeVisible();
  await expect(page.getByText('Tester Two')).toBeVisible();

  await page.locator('.score-card.active .score-control button').filter({ hasText: '+' }).click();

  await expect(page.locator('.score-card.active .score-control strong')).toHaveText('1');

  await page.getByRole('button', { name: 'Next Round' }).click();

  await expect(page.getByText('02/05')).toBeVisible();

  await page.getByRole('button', { name: 'Save Match' }).click();

  const matchSummary = page.locator('article').filter({
    has: page.getByRole('heading', { name: 'Match Summary' })
  });

  await expect(matchSummary.getByRole('heading', { name: 'Match Summary' })).toBeVisible();
  await expect(matchSummary.getByText('Final Score')).toBeVisible();
  await expect(matchSummary.locator('strong').filter({ hasText: /^1 - 0$/ }).first()).toBeVisible();

  await page.getByRole('button', { name: 'Confirm Save' }).click();

  await expect(page).toHaveURL(/\/matches$/);
  await expect(page.getByRole('heading', { name: 'Saved Matches' })).toBeVisible();
  await expect(page.getByText('Space Marines')).toBeVisible();
  await expect(page.getByText('Necrons')).toBeVisible();
  await expect(page.getByText('1 - 0', { exact: true }).first()).toBeVisible();
});

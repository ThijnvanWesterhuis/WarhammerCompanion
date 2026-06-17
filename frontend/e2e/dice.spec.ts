import { expect, test } from '@playwright/test';
import { registerAndLoginByApi } from './helpers/auth';

test('logged in user can roll dice with a success threshold', async ({ page, request }) => {
  await registerAndLoginByApi(page, request, undefined);

  await page.goto('/dice');

  await expect(page.getByRole('heading', { name: 'Combat Resolution' })).toBeVisible();

  await page.locator('#diceType').selectOption('D10');
  await page.locator('#diceCount').fill('3');
  await page.getByRole('button', { name: '5+' }).click();
  await page.getByRole('button', { name: /Execute Roll/ }).click();

  await expect(page.getByText('Rolled 3 D10 dice.')).toBeVisible();
  await expect(page.getByText('Volley: 3 | Target: 5+')).toBeVisible();
  await expect(page.locator('.results-panel .die-result')).toHaveCount(3);
  await expect(page.locator('.activity-panel .history-row').first()).toContainText('3x D10');
});

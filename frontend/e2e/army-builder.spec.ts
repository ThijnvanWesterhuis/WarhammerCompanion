import { expect, test } from '@playwright/test';
import { registerAndLoginByApi } from './helpers/auth';

test('user can create a 10th edition army list from catalog units', async ({ page, request }) => {
  const armyListName = `Ultramarines E2E ${Date.now()}`;

  await registerAndLoginByApi(page, request, undefined);
  await page.goto('/army-lists');

  await expect(page.getByRole('heading', { name: 'Army Builder' })).toBeVisible();

  await page.locator('#name').fill(armyListName);
  await page.locator('#gameEditionCode').selectOption('10TH');
  await page.locator('#faction').selectOption('Ultramarines');
  await page.locator('#armyRule').selectOption('Gladius Task Force');
  await page.locator('#pointsLimit').selectOption('500');

  await selectCatalogUnit(page, 'Captain');
  await page.getByRole('button', { name: 'Add Unit' }).click();

  await selectCatalogUnit(page, 'Intercessor Squad');
  await page.getByRole('button', { name: 'Add Unit' }).click();

  await selectCatalogUnit(page, 'Intercessor Squad');
  await page.getByRole('button', { name: 'Add Unit' }).click();

  await expect(page.getByText('240')).toBeVisible();
  await expect(page.getByText('260')).toBeVisible();

  await page.getByRole('button', { name: 'Create Army List' }).click();

  await expect(page.getByText('Army list created')).toBeVisible();
  await expect(page.locator('.army-list-card').filter({ hasText: armyListName })).toBeVisible();
  await expect(page.locator('.army-list-card').filter({ hasText: '240' })).toBeVisible();
  await expect(page.locator('.army-list-card').filter({ hasText: '/ 500 pts' })).toBeVisible();
});

test('10th edition army builder rejects duplicate epic heroes', async ({ page, request }) => {
  const armyListName = `Invalid Epic Hero ${Date.now()}`;

  await registerAndLoginByApi(page, request, undefined);
  await page.goto('/army-lists');

  await expect(page.getByRole('heading', { name: 'Army Builder' })).toBeVisible();

  await page.locator('#name').fill(armyListName);
  await page.locator('#gameEditionCode').selectOption('10TH');
  await page.locator('#faction').selectOption('Ultramarines');
  await page.locator('#armyRule').selectOption('Gladius Task Force');
  await page.locator('#pointsLimit').selectOption('500');

  await selectCatalogUnit(page, 'Captain');
  await page.getByRole('button', { name: 'Add Unit' }).click();

  await selectCatalogUnit(page, 'Marneus Calgar');
  await page.getByRole('button', { name: 'Add Unit' }).click();

  await selectCatalogUnit(page, 'Marneus Calgar');
  await page.getByRole('button', { name: 'Add Unit' }).click();

  await page.getByRole('button', { name: 'Create Army List' }).click();

  await expect(
    page.getByText('Marneus Calgar exceeds the 10th edition datasheet limit of 1')
  ).toBeVisible();
});

test('user can filter saved army lists by search, faction and edition', async ({ page, request }) => {
  const armyListName = `Filter Army ${Date.now()}`;

  await registerAndLoginByApi(page, request, undefined);
  await page.goto('/army-lists');

  await page.locator('#name').fill(armyListName);
  await page.locator('#gameEditionCode').selectOption('10TH');
  await page.locator('#faction').selectOption('Ultramarines');
  await page.locator('#armyRule').selectOption('Gladius Task Force');
  await page.locator('#pointsLimit').selectOption('500');

  await selectCatalogUnit(page, 'Captain');
  await page.getByRole('button', { name: 'Add Unit' }).click();

  await selectCatalogUnit(page, 'Intercessor Squad');
  await page.getByRole('button', { name: 'Add Unit' }).click();

  await page.getByRole('button', { name: 'Create Army List' }).click();

  await expect(page.getByText('Army list created')).toBeVisible();

  await page.locator('#search').fill(armyListName);
  await page.locator('#factionFilter').selectOption('Ultramarines');
  await page.locator('#editionFilter').selectOption('10TH');
  await page.getByRole('button', { name: 'Apply' }).click();

  await expect(page.locator('.army-list-card')).toHaveCount(1);
  await expect(page.locator('.army-list-card').filter({ hasText: armyListName })).toBeVisible();
  await expect(page.locator('.army-list-card').filter({ hasText: 'Ultramarines' })).toBeVisible();
  await expect(page.locator('.army-list-card').filter({ hasText: '10th Edition' })).toBeVisible();
});

async function selectCatalogUnit(page, unitName: string) {
  const option = page
    .locator('#selectedCatalogUnitId option')
    .filter({ hasText: unitName })
    .first();

  await expect(option).toBeVisible();

  const value = await option.getAttribute('value');

  if (!value) {
    throw new Error(`Could not find catalog unit option value for ${unitName}`);
  }

  await page.locator('#selectedCatalogUnitId').selectOption(value);
}

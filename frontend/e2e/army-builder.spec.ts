import { expect, Page, test } from '@playwright/test';
import { registerAndLoginByApi } from './helpers/auth';

test('user can create a 10th edition army list from catalog units', async ({ page, request }) => {
  const armyListName = `Ultramarines E2E ${Date.now()}`;

  await registerAndLoginByApi(page, request, undefined);
  await page.goto('/army-lists');

  await expect(page.getByRole('heading', { name: 'Army Builder' })).toBeVisible();

  await page.locator('#name').fill(armyListName);
  await page.locator('#gameEditionCode').selectOption({ label: '10th Edition' });
  await page.locator('#faction').selectOption({ label: 'Ultramarines' });
  await page.locator('#armyRule').selectOption({ label: 'Gladius Task Force' });
  await page.locator('#pointsLimit').selectOption({ label: '500 points' });

  await selectCatalogUnit(page, 'Captain');
  await page.getByRole('button', { name: /^Add Unit$/ }).click();

  await selectCatalogUnit(page, 'Intercessor Squad');
  await page.getByRole('button', { name: /^Add Unit$/ }).click();

  await selectCatalogUnit(page, 'Intercessor Squad');
  await page.getByRole('button', { name: /^Add Unit$/ }).click();

  await expect(page.getByText('240', { exact: true }).first()).toBeVisible();
  await expect(page.getByText('260', { exact: true }).first()).toBeVisible();

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
  await page.locator('#gameEditionCode').selectOption({ label: '10th Edition' });
  await page.locator('#faction').selectOption({ label: 'Ultramarines' });
  await page.locator('#armyRule').selectOption({ label: 'Gladius Task Force' });
  await page.locator('#pointsLimit').selectOption({ label: '500 points' });

  await selectCatalogUnit(page, 'Captain');
  await page.getByRole('button', { name: /^Add Unit$/ }).click();

  await selectCatalogUnit(page, 'Marneus Calgar');
  await page.getByRole('button', { name: /^Add Unit$/ }).click();

  await selectCatalogUnit(page, 'Marneus Calgar');
  await page.getByRole('button', { name: /^Add Unit$/ }).click();

  await page.getByRole('button', { name: 'Create Army List' }).click();

  await expect(
    page.getByText('Marneus Calgar exceeds the 10th edition datasheet limit of 1')
  ).toBeVisible();
});

test('user can filter saved army lists by search, faction and edition', async ({ page, request }) => {
  const armyListName = `Filter Army ${Date.now()}`;

  await registerAndLoginByApi(page, request, undefined);
  await page.goto('/army-lists');

  await expect(page.getByRole('heading', { name: 'Army Builder' })).toBeVisible();

  await page.locator('#name').fill(armyListName);
  await page.locator('#gameEditionCode').selectOption({ label: '10th Edition' });
  await page.locator('#faction').selectOption({ label: 'Ultramarines' });
  await page.locator('#armyRule').selectOption({ label: 'Gladius Task Force' });
  await page.locator('#pointsLimit').selectOption({ label: '500 points' });

  await selectCatalogUnit(page, 'Captain');
  await page.getByRole('button', { name: /^Add Unit$/ }).click();

  await selectCatalogUnit(page, 'Intercessor Squad');
  await page.getByRole('button', { name: /^Add Unit$/ }).click();

  await page.getByRole('button', { name: 'Create Army List' }).click();

  await expect(page.getByText('Army list created')).toBeVisible();

  await page.locator('#search').fill(armyListName);
  await page.locator('#factionFilter').selectOption({ label: 'Ultramarines' });
  await page.locator('#editionFilter').selectOption({ label: '10th Edition' });
  await page.getByRole('button', { name: 'Apply' }).click();

  await expect(page.locator('.army-list-card')).toHaveCount(1);
  await expect(page.locator('.army-list-card').filter({ hasText: armyListName })).toBeVisible();
  await expect(page.locator('.army-list-card').filter({ hasText: 'Ultramarines' })).toBeVisible();
  await expect(page.locator('.army-list-card').filter({ hasText: '10th Edition' })).toBeVisible();
});

async function selectCatalogUnit(page: Page, unitName: string) {
  const catalogSelect = page.locator('#selectedCatalogUnitId');

  await expect(catalogSelect).toBeVisible();
  await waitForCatalogOptions(page);

  const optionValue = await catalogSelect.evaluate(
    (selectElement, wantedUnitName) => {
      const select = selectElement as HTMLSelectElement;

      const matchingOption = Array.from(select.options).find(option =>
        option.textContent?.trim().startsWith(`${wantedUnitName} //`)
      );

      return matchingOption?.value ?? null;
    },
    unitName
  );

  if (!optionValue) {
    throw new Error(`Could not find catalog unit option for ${unitName}`);
  }

  await catalogSelect.selectOption(optionValue);
}

async function waitForCatalogOptions(page: Page) {
  await expect.poll(async () => {
    return await page.locator('#selectedCatalogUnitId option').count();
  }).toBeGreaterThan(1);
}

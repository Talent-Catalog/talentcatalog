import {expect, test} from '@playwright/test';

test('Chromium performance runner starts (infrastructure only)', async ({page}) => {
  // This is NOT a performance journey and does not call staging.
  // It proves that the isolated Playwright runner can launch Chromium.
  await page.setContent('<main><h1>TC performance runner ready</h1></main>');

  await expect(page.getByRole('heading', {
    name: 'TC performance runner ready',
  })).toBeVisible();
});

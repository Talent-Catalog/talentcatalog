/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {test,} from '@playwright/test';

import {VERIFY_PLUS_HIGH_DENSITY_QR_FIXTURES,} from '../fixtures/verify-plus-qr.fixture';

import {runVerifyPlusQrDecodingScenario,} from '../support/verify-plus-qr-decoding.e2e';

/**
 * Cross-browser decoding coverage for high-density Verify+ QR images.
 *
 * These tests use the same real application decoding path as the smaller
 * synthetic fixtures, while exercising substantially denser QR symbols.
 *
 * Full decoded values are intentionally not logged or hardcoded because they
 * contain large encoded sections. Stable document markers and UNHCR IDs are
 * asserted through the shared fixture metadata instead.
 */
test.describe(
  'Verify+ high-density QR decoding',
  () => {
    for (
      const fixture
      of VERIFY_PLUS_HIGH_DENSITY_QR_FIXTURES
      ) {
      test(
        `decodes ${fixture.fileName} with zxing-wasm`,
        async ({
                 page,
               }, testInfo) => {
          /*
           * Leave enough time for cold WebKit and zxing-wasm initialization.
           */
          test.setTimeout(
            120_000,
          );

          await runVerifyPlusQrDecodingScenario(
            page,
            testInfo.config.testDir,
            fixture,
          );
        },
      );
    }
  },
);

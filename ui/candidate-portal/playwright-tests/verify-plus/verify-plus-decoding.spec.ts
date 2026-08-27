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

import {VERIFY_PLUS_DOCUMENTED_QR_FIXTURES,} from '../fixtures/verify-plus-qr.fixture';

import {runVerifyPlusQrDecodingScenario,} from '../support/verify-plus-qr-decoding.e2e';

/**
 * Verify+ decoding coverage for the documented synthetic QR fixtures.
 *
 * Payload validity is intentionally not evaluated here.
 *
 * A QR containing an invalid version, malformed JSON or non-JSON text should
 * still be readable by the QR decoder. Business validation belongs to a
 * separate confirmation/submission E2E suite.
 */
test.describe(
  'Verify+ QR decoding',
  () => {
    for (
      const fixture
      of VERIFY_PLUS_DOCUMENTED_QR_FIXTURES
      ) {
      test(
        `decodes ${fixture.fileName} with zxing-wasm`,
        async ({
                 page,
               }, testInfo) => {
          /*
           * WebKit and WASM startup can be slower on a cold browser process.
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

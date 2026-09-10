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

import {readFile,} from 'node:fs/promises';

import {join,} from 'node:path';

import {expect,} from '@playwright/test';

import {VerifyPlusQrFixture,} from '../fixtures/verify-plus-qr.fixture';

/**
 * Location of the shared Verify+ QR assets relative to Playwright's test root.
 *
 * Playwright's configured root is:
 *
 * `ui/candidate-portal/playwright-tests`
 *
 * while the reusable QR images live under:
 *
 * `ui/candidate-portal/docs/verify-plus`
 */
const VERIFY_PLUS_QR_DIRECTORY_FROM_TEST_ROOT = [
  '..',
  'docs',
  'verify-plus',
] as const;

/**
 * Standard eight-byte PNG file signature.
 *
 * Checking this before passing fixture data to the browser produces a clear
 * test error if a file is missing, corrupted or accidentally replaced with a
 * different format.
 */
const PNG_SIGNATURE =
  Buffer.from([
    0x89,
    0x50,
    0x4e,
    0x47,
    0x0d,
    0x0a,
    0x1a,
    0x0a,
  ]);

/**
 * Resolves the filesystem location of a Verify+ QR fixture.
 *
 * Keeping path resolution in one helper means decoder, validation and
 * submission tests can all reuse the same files without duplicating knowledge
 * of the `docs/verify-plus` directory.
 *
 * @param playwrightRootDir Playwright test root directory
 * @param fixture QR fixture metadata
 * @returns absolute path to the PNG fixture
 */
export function resolveVerifyPlusQrFixturePath(
  playwrightRootDir: string,
  fixture: VerifyPlusQrFixture,
): string {
  return join(
    playwrightRootDir,
    ...VERIFY_PLUS_QR_DIRECTORY_FROM_TEST_ROOT,
    fixture.fileName,
  );
}

/**
 * Loads a Verify+ PNG fixture and converts it to a browser-safe data URL.
 *
 * Playwright test code runs in Node, while the camera mock executes inside the
 * browser page. A base64 data URL provides a simple deterministic way to
 * transfer the original PNG bytes across that boundary.
 *
 * The helper verifies that:
 *
 * - the file is not empty;
 * - the file contains a valid PNG signature.
 *
 * @param playwrightRootDir Playwright test root directory
 * @param fixture QR fixture metadata
 * @returns base64 PNG data URL
 * @throws Error when the fixture is empty or is not a PNG
 */
export async function loadVerifyPlusQrFixtureAsDataUrl(
  playwrightRootDir: string,
  fixture: VerifyPlusQrFixture,
): Promise<string> {
  const fixturePath =
    resolveVerifyPlusQrFixturePath(
      playwrightRootDir,
      fixture,
    );

  const pngBytes =
    await readFile(
      fixturePath,
    );

  if (
    pngBytes.length === 0
  ) {
    throw new Error(
      `Verify+ QR fixture is empty: ${fixture.fileName}`,
    );
  }

  if (
    pngBytes.length <
    PNG_SIGNATURE.length ||
    !pngBytes
    .subarray(
      0,
      PNG_SIGNATURE.length,
    )
    .equals(
      PNG_SIGNATURE,
    )
  ) {
    throw new Error(
      `Verify+ QR fixture is not a valid PNG: ${fixture.fileName}`,
    );
  }

  return (
    'data:image/png;base64,' +
    pngBytes.toString(
      'base64',
    )
  );
}

/**
 * Verifies decoded browser output against a QR fixture's declared expectation.
 *
 * JSON fixtures are compared semantically rather than as formatted strings
 * because Verify+ pretty-prints valid JSON before displaying it.
 *
 * Malformed and non-JSON fixtures are compared as exact text.
 *
 * High-density fixtures use stable substring assertions so very large decoded
 * values do not need to be copied into test source or CI output.
 *
 * @param decodedPayload text displayed by the Verify+ review UI
 * @param fixture fixture that produced the decoded payload
 */
export function expectVerifyPlusDecodedFixture(
  decodedPayload: string,
  fixture: VerifyPlusQrFixture,
): void {
  const expected =
    fixture.expected;

  switch (
    expected.kind
    ) {
    case 'json': {
      let parsedPayload:
        unknown;

      try {
        parsedPayload =
          JSON.parse(
            decodedPayload,
          );
      } catch {
        throw new Error(
          `Expected ${fixture.fileName} to decode to valid JSON, ` +
          `but received: ${decodedPayload}`,
        );
      }

      expect(
        parsedPayload,
        `Expected ${fixture.fileName} to decode to the documented JSON payload`,
      ).toEqual(
        expected.value,
      );

      return;
    }

    case 'text': {
      expect(
        decodedPayload.trim(),
        `Expected ${fixture.fileName} to decode to the documented text`,
      ).toBe(
        expected.value,
      );

      return;
    }

    case 'contains': {
      expect(
        decodedPayload.trim(),
        `Expected ${fixture.fileName} to produce decoded text`,
      ).not.toBe('');

      for (
        const expectedValue
        of expected.values
        ) {
        expect(
          decodedPayload,
          `Expected ${fixture.fileName} to contain "${expectedValue}"`,
        ).toContain(
          expectedValue,
        );
      }

      return;
    }
  }
}

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

/**
 * Business scenario represented by a Verify+ QR fixture.
 */
export type VerifyPlusQrScenario =
  | 'valid'
  | 'valid-duplicate'
  | 'invalid-version'
  | 'missing-unhcr-id'
  | 'malformed-json'
  | 'non-json'
  | 'high-density';

/**
 * Expected result of decoding the QR image.
 */
export type VerifyPlusQrDecodeExpectation =
  | {
  readonly kind: 'json';
  readonly value: Readonly<Record<string, unknown>>;
}
  | {
  readonly kind: 'text';
  readonly value: string;
}
  | {
  readonly kind: 'contains';
  readonly values: readonly string[];
};

/**
 * Expected business-validation result after the user confirms a decoded scan.
 *
 * `success`
 *     The backend accepts the payload and assigns the UNHCR number.
 *
 * `duplicate`
 *     The payload is valid, but the UNHCR number already belongs to another
 *     active-like candidate.
 *
 * `error`
 *     The QR was successfully decoded, but its decoded payload is not accepted
 *     by the Verify+ backend contract.
 */
export type VerifyPlusQrValidationExpectation =
  | {
  readonly kind: 'success';
  readonly unhcrId: string;
}
  | {
  readonly kind: 'duplicate';
  readonly unhcrId: string;
}
  | {
  readonly kind: 'error';
  readonly message: string;
};

/**
 * Base metadata for any reusable Verify+ QR fixture.
 *
 * Every PNG lives under:
 *
 * `docs/verify-plus`
 */
export interface VerifyPlusQrFixture {
  /**
   * Stable identifier used by test code.
   */
  readonly id: string;

  /**
   * PNG file name under `docs/verify-plus`.
   */
  readonly fileName: string;

  /**
   * Business/testing scenario represented by the image.
   */
  readonly scenario: VerifyPlusQrScenario;

  /**
   * Expected raw decoding behavior.
   */
  readonly expected: VerifyPlusQrDecodeExpectation;
}

/**
 * QR fixture that can also be submitted to the current Verify+ mock backend
 * contract and has a known validation result.
 */
export interface VerifyPlusQrValidationFixture
  extends VerifyPlusQrFixture {
  /**
   * Expected browser/backend result after Confirm is clicked.
   */
  readonly validation: VerifyPlusQrValidationExpectation;
}

/**
 * Small documented QR fixtures.
 *
 * All six images should first decode successfully. Their decoded payloads are
 * then submitted in the validation E2E suite to verify backend/business
 * behavior.
 */
export const VERIFY_PLUS_DOCUMENTED_QR_FIXTURES = [
  {
    id: 'valid',

    fileName:
      'verify-plus-valid.png',

    scenario:
      'valid',

    expected: {
      kind: 'json',

      value: {
        v:
          'mock-1',

        unhcrId:
          '123-45C67890',
      },
    },

    validation: {
      kind:
        'success',

      unhcrId:
        '123-45C67890',
    },
  },

  {
    id:
      'valid-duplicate',

    fileName:
      'verify-plus-valid-duplicate.png',

    scenario:
      'valid-duplicate',

    expected: {
      kind:
        'json',

      value: {
        v:
          'mock-1',

        unhcrId:
          '999-00A11111',
      },
    },
    /*
     * Browser duplicate validation is only runnable in environments where
     * another active-like candidate owns this exact static fixture ID.
     *
     * Other environments skip the browser duplicate scenario while retaining
     * backend duplicate coverage in verify-plus-submission.api.spec.ts.
     */
    validation: {
      kind:
        'duplicate',

      unhcrId:
        '999-00A11111',
    },
  },

  {
    id:
      'invalid-version',

    fileName:
      'verify-plus-invalid-version.png',

    scenario:
      'invalid-version',

    expected: {
      kind:
        'json',

      value: {
        v:
          'mock-2',

        unhcrId:
          '123-45C67890',
      },
    },

    validation: {
      kind:
        'error',

      message:
        'Unsupported Verify+ payload version: mock-2',
    },
  },

  {
    id:
      'missing-unhcr-id',

    fileName:
      'verify-plus-missing-unhcr-id.png',

    scenario:
      'missing-unhcr-id',

    expected: {
      kind:
        'json',

      value: {
        v:
          'mock-1',
      },
    },

    validation: {
      kind:
        'error',

      message:
        'Missing field: unhcrId',
    },
  },

  {
    id:
      'malformed-json',

    fileName:
      'verify-plus-malformed-json.png',

    scenario:
      'malformed-json',

    expected: {
      kind:
        'text',

      value:
        '{"v":"mock-1","unhcrId":"123-45C67890"',
    },

    validation: {
      kind:
        'error',

      message:
        'Malformed Verify+ payload',
    },
  },

  {
    id:
      'non-json',

    fileName:
      'verify-plus-non-json.png',

    scenario:
      'non-json',

    expected: {
      kind:
        'text',

      value:
        'hello world',
    },

    validation: {
      kind:
        'error',

      message:
        'Malformed Verify+ payload',
    },
  },
] as const satisfies
  readonly VerifyPlusQrValidationFixture[];

/**
 * High-density QR fixtures.
 *
 * These deliberately have no submission expectation yet because the current
 * backend parser accepts the temporary `mock-1` JSON contract, while these
 * images contain a different high-density UNHCR payload format.
 */
export const VERIFY_PLUS_HIGH_DENSITY_QR_FIXTURES = [
  {
    id:
      'ken-123a-113097921',

    fileName:
      'QR_KEN_123A-113097921.png',

    scenario:
      'high-density',

    expected: {
      kind:
        'contains',

      values: [
        'RSDLETTER2025',
        '123A-113097921',
      ],
    },
  },

  {
    id:
      'ken-803-100073256',

    fileName:
      'QR_KEN_803-100073256.png',

    scenario:
      'high-density',

    expected: {
      kind:
        'contains',

      values: [
        'RSDLETTER2025',
        '803-100073256',
      ],
    },
  },

  {
    id:
      'ken-803-100110602',

    fileName:
      'QR_KEN_803-100110602.png',

    scenario:
      'high-density',

    expected: {
      kind:
        'contains',

      values: [
        'RSDLETTER2025',
        '803-100110602',
      ],
    },
  },

  {
    id:
      'ken-803-100113902',

    fileName:
      'QR_KEN_803-100113902.png',

    scenario:
      'high-density',

    expected: {
      kind:
        'contains',

      values: [
        'RSDLETTER2025',
        '803-100113902',
      ],
    },
  },
] as const satisfies
  readonly VerifyPlusQrFixture[];

/**
 * Fixtures participating in browser-side Verify+ validation.
 */
export const VERIFY_PLUS_VALIDATION_QR_FIXTURES =
  VERIFY_PLUS_DOCUMENTED_QR_FIXTURES;

/**
 * All QR fixtures used by automated E2E coverage.
 */
export const VERIFY_PLUS_ALL_QR_FIXTURES = [
  ...VERIFY_PLUS_DOCUMENTED_QR_FIXTURES,
  ...VERIFY_PLUS_HIGH_DENSITY_QR_FIXTURES,
] as const;

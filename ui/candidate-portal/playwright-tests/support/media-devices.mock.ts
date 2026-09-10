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

import {Page} from '@playwright/test';

const GET_USER_MEDIA_CALL_COUNT =
  '__verifyPlusGetUserMediaCallCount';

type MediaDevicesScenario =
  | 'permission-denied'
  | 'no-camera'
  | 'unexpected-error';

/**
 * Installs a deterministic MediaDevices implementation before application
 * scripts execute.
 *
 * @param page Playwright page receiving the browser API mock
 * @param scenario simulated camera scenario
 */
async function installMediaDevicesMock(
  page: Page,
  scenario: MediaDevicesScenario,
): Promise<void> {
  await page.addInitScript(
    ({
       callCountProperty,
       selectedScenario,
     }) => {
      const testWindow = window as typeof window & {
        [key: string]: unknown;
      };

      testWindow[callCountProperty] = 0;

      /**
       * Creates a synthetic camera-device description.
       *
       * @returns mock camera device
       */
      function createCameraDevice(): MediaDeviceInfo {
        return {
          deviceId: 'verify-plus-e2e-camera',
          groupId: 'verify-plus-e2e-group',
          kind: 'videoinput',
          label: 'Verify+ E2E camera',

          toJSON() {
            return {
              deviceId: this.deviceId,
              groupId: this.groupId,
              kind: this.kind,
              label: this.label,
            };
          },
        };
      }

      const mockedMediaDevices = {
        /**
         * Returns devices for the selected test scenario.
         */
        async enumerateDevices(): Promise<MediaDeviceInfo[]> {
          if (selectedScenario === 'no-camera') {
            return [];
          }

          return [createCameraDevice()];
        },

        /**
         * Handles camera requests for the selected test scenario.
         */
        async getUserMedia(): Promise<MediaStream> {
          const currentCount =
            Number(testWindow[callCountProperty]) || 0;

          testWindow[callCountProperty] =
            currentCount + 1;

          if (selectedScenario === 'permission-denied') {
            throw new DOMException(
              'Camera permission denied by Verify+ E2E test.',
              'NotAllowedError',
            );
          }

          if (selectedScenario === 'unexpected-error') {
            throw new Error(
              'Unexpected camera initialization failure from Verify+ E2E test.',
            );
          }

          /*
           * The no-camera scenario should return before getUserMedia is
           * reached. Throwing here makes an unexpected application call
           * immediately visible.
           */
          throw new Error(
            'getUserMedia was called when no camera devices were available.',
          );
        },
      };

      Object.defineProperty(
        navigator,
        'mediaDevices',
        {
          configurable: true,
          enumerable: true,
          value: mockedMediaDevices,
        },
      );
    },
    {
      callCountProperty: GET_USER_MEDIA_CALL_COUNT,
      selectedScenario: scenario,
    },
  );
}

/**
 * Reports one camera but denies access to it.
 *
 * @param page Playwright page receiving the browser API mock
 */
export async function installCameraPermissionDeniedMock(
  page: Page,
): Promise<void> {
  await installMediaDevicesMock(
    page,
    'permission-denied',
  );
}

/**
 * Reports that the browser has no video-input devices.
 *
 * @param page Playwright page receiving the browser API mock
 */
export async function installNoCameraDevicesMock(
  page: Page,
): Promise<void> {
  await installMediaDevicesMock(
    page,
    'no-camera',
  );
}

/**
 * Reports one camera but simulates an unexpected browser or device failure
 * while attempting to initialize the media stream.
 *
 * @param page Playwright page receiving the browser API mock
 */
export async function installUnexpectedCameraErrorMock(
  page: Page,
): Promise<void> {
  await installMediaDevicesMock(
    page,
    'unexpected-error',
  );
}

/**
 * Returns how many times the application requested camera access.
 *
 * @param page Playwright page containing the installed browser API mock
 * @returns number of getUserMedia calls made by the scanner
 */
export async function getUserMediaCallCount(
  page: Page,
): Promise<number> {
  return page.evaluate(
    ({callCountProperty}) => {
      const testWindow = window as typeof window & {
        [key: string]: unknown;
      };

      return Number(
        testWindow[callCountProperty],
      ) || 0;
    },
    {
      callCountProperty: GET_USER_MEDIA_CALL_COUNT,
    },
  );
}

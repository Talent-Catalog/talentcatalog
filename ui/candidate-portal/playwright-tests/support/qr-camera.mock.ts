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

import {Page,} from '@playwright/test';

/**
 * Browser-global property containing QR camera diagnostics.
 */
const DIAGNOSTICS_PROPERTY =
  '__verifyPlusQrCameraDiagnostics';

/**
 * Runtime diagnostics for the deterministic Verify+ camera.
 *
 * Successful E2E tests normally inspect only `injectedFrames`. The additional
 * counters remain available to diagnose browser-specific media failures.
 */
export interface VerifyPlusQrCameraDiagnostics {
  /**
   * Number of getUserMedia() calls handled by the camera mock.
   */
  getUserMediaCalls: number;

  /**
   * Number of assignments to the scanner video's srcObject.
   */
  srcObjectSets: number;

  /**
   * Number of scanner-video play() calls.
   */
  playCalls: number;

  /**
   * Number of scanner-video pause() calls.
   */
  pauseCalls: number;

  /**
   * Number of reads of the scanner video's intrinsic width.
   */
  videoWidthReads: number;

  /**
   * Number of reads of the scanner video's intrinsic height.
   */
  videoHeightReads: number;

  /**
   * Number of QR image frames copied into the production capture canvas.
   */
  injectedFrames: number;
}

/**
 * Installs a deterministic Verify+ camera that presents a known PNG fixture.
 *
 * Only the physical camera and native media-playback boundary are replaced.
 *
 * Production application behavior remains responsible for:
 *
 * scanner interval
 * -> captureFrame()
 * -> capture canvas
 * -> getImageData()
 * -> VerifyPlusDecoderService
 * -> zxing-wasm/WASM
 * -> Angular scanned event
 * -> decoded-payload UI
 *
 * The browser loads the original PNG into an HTMLImageElement and paints it
 * onto a normal canvas. When production captureFrame() later draws the scanner
 * video into its own capture canvas, only the image source is substituted with
 * the fixture canvas.
 *
 * getImageData() is never mocked.
 *
 * A lightweight MediaStream-shaped object is returned instead of constructing
 * an empty native MediaStream. This keeps Chromium and WebKit behavior
 * deterministic while still satisfying production cleanup through getTracks().
 *
 * @param page Playwright page containing the rendered Verify+ scanner
 * @param qrPngDataUrl PNG fixture represented as a base64 data URL
 */
export async function installQrCameraMock(
  page: Page,
  qrPngDataUrl: string,
): Promise<void> {
  if (
    !qrPngDataUrl.startsWith(
      'data:image/png;base64,',
    )
  ) {
    throw new Error(
      'Verify+ QR camera requires a base64 PNG data URL.',
    );
  }

  await page.evaluate(
    async ({
             imageDataUrl,
             diagnosticsProperty,
           }) => {
      const scanner =
        document.querySelector(
          'app-verify-plus-scanner',
        );

      if (!scanner) {
        throw new Error(
          'Verify+ QR camera could not find the scanner component.',
        );
      }

      const video =
        scanner.querySelector<HTMLVideoElement>(
          'video',
        );

      if (!video) {
        throw new Error(
          'Verify+ QR camera could not find the scanner video.',
        );
      }

      const captureCanvas =
        scanner.querySelector<HTMLCanvasElement>(
          'canvas.capture-canvas',
        );

      if (!captureCanvas) {
        throw new Error(
          'Verify+ QR camera could not find the scanner capture canvas.',
        );
      }

      const testWindow =
        window as typeof window & {
          [key: string]: unknown;
        };

      const diagnostics:
        VerifyPlusQrCameraDiagnostics = {
        getUserMediaCalls:
          0,

        srcObjectSets:
          0,

        playCalls:
          0,

        pauseCalls:
          0,

        videoWidthReads:
          0,

        videoHeightReads:
          0,

        injectedFrames:
          0,
      };

      testWindow[
        diagnosticsProperty
        ] = diagnostics;

      /*
       * Decode the original PNG using the current browser engine.
       */
      const qrImage =
        new Image();

      await new Promise<void>(
        (
          resolve,
          reject,
        ) => {
          qrImage.onload =
            () => resolve();

          qrImage.onerror =
            () => reject(
              new Error(
                'Verify+ QR camera could not load the PNG fixture.',
              ),
            );

          qrImage.src =
            imageDataUrl;
        },
      );

      if (
        qrImage.naturalWidth === 0 ||
        qrImage.naturalHeight === 0
      ) {
        throw new Error(
          'Verify+ QR fixture has invalid image dimensions.',
        );
      }

      /*
       * Create a genuine browser canvas containing the fixture pixels.
       */
      const qrCanvas =
        document.createElement(
          'canvas',
        );

      qrCanvas.width =
        qrImage.naturalWidth;

      qrCanvas.height =
        qrImage.naturalHeight;

      const qrContext =
        qrCanvas.getContext(
          '2d',
          {
            willReadFrequently:
              true,
          },
        );

      if (!qrContext) {
        throw new Error(
          'Verify+ QR camera could not create a fixture canvas context.',
        );
      }

      qrContext.drawImage(
        qrImage,
        0,
        0,
        qrCanvas.width,
        qrCanvas.height,
      );

      /**
       * Creates the camera returned to production enumerateDevices().
       *
       * @returns deterministic video-input device
       */
      function createCameraDevice():
        MediaDeviceInfo {
        return {
          deviceId:
            'verify-plus-e2e-qr-camera',

          groupId:
            'verify-plus-e2e-group',

          kind:
            'videoinput',

          label:
            'Verify+ E2E QR camera',

          toJSON() {
            return {
              deviceId:
              this.deviceId,

              groupId:
              this.groupId,

              kind:
              this.kind,

              label:
              this.label,
            };
          },
        };
      }

      const mockedMediaDevices = {
        /**
         * Reports one synthetic camera.
         *
         * @returns deterministic video-input device
         */
        async enumerateDevices():
          Promise<MediaDeviceInfo[]> {
          return [
            createCameraDevice(),
          ];
        },

        /**
         * Grants synthetic camera access.
         *
         * The returned object only needs getTracks() because the production
         * scanner stores it and later stops its tracks during cleanup.
         *
         * @returns MediaStream-shaped object
         */
        async getUserMedia():
          Promise<MediaStream> {
          diagnostics
            .getUserMediaCalls +=
            1;

          return {
            getTracks:
              (): MediaStreamTrack[] =>
                [],
          } as unknown as MediaStream;
        },
      };

      Object.defineProperty(
        navigator,
        'mediaDevices',
        {
          configurable:
            true,

          enumerable:
            true,

          value:
          mockedMediaDevices,
        },
      );

      /*
       * Keep the synthetic stream out of the native browser media pipeline.
       */
      let assignedSrcObject:
        unknown = null;

      Object.defineProperty(
        video,
        'srcObject',
        {
          configurable:
            true,

          enumerable:
            true,

          get:
            () =>
              assignedSrcObject,

          set:
            (
              value: unknown,
            ): void => {
              diagnostics
                .srcObjectSets +=
                1;

              assignedSrcObject =
                value;
            },
        },
      );

      /*
       * Production awaits play() before starting its actual decode loop.
       */
      Object.defineProperty(
        video,
        'play',
        {
          configurable:
            true,

          writable:
            true,

          value:
            (): Promise<void> => {
              diagnostics
                .playCalls +=
                1;

              return Promise.resolve();
            },
        },
      );

      /*
       * Keep production video cleanup deterministic.
       */
      Object.defineProperty(
        video,
        'pause',
        {
          configurable:
            true,

          writable:
            true,

          value:
            (): void => {
              diagnostics
                .pauseCalls +=
                1;
            },
        },
      );

      const nativeVideoWidthDescriptor =
        Object.getOwnPropertyDescriptor(
          HTMLVideoElement.prototype,
          'videoWidth',
        );

      const nativeVideoHeightDescriptor =
        Object.getOwnPropertyDescriptor(
          HTMLVideoElement.prototype,
          'videoHeight',
        );

      /*
       * Production captureFrame() requires non-zero intrinsic dimensions.
       */
      Object.defineProperty(
        HTMLVideoElement.prototype,
        'videoWidth',
        {
          configurable:
            true,

          get:
            function ():
              number {
              if (
                this === video
              ) {
                diagnostics
                  .videoWidthReads +=
                  1;

                return qrCanvas.width;
              }

              return (
                nativeVideoWidthDescriptor
                ?.get
                ?.call(this) ??
                0
              );
            },
        },
      );

      Object.defineProperty(
        HTMLVideoElement.prototype,
        'videoHeight',
        {
          configurable:
            true,

          get:
            function ():
              number {
              if (
                this === video
              ) {
                diagnostics
                  .videoHeightReads +=
                  1;

                return qrCanvas.height;
              }

              return (
                nativeVideoHeightDescriptor
                ?.get
                ?.call(this) ??
                0
              );
            },
        },
      );

      const nativeDrawImage =
        CanvasRenderingContext2D
          .prototype
          .drawImage;

      /*
       * Replace only the scanner-video source used by production
       * captureFrame(). The application's capture canvas remains real.
       */
      Object.defineProperty(
        CanvasRenderingContext2D
          .prototype,
        'drawImage',
        {
          configurable:
            true,

          writable:
            true,

          value:
            function (
              this:
                CanvasRenderingContext2D,

              image:
                CanvasImageSource,

              ...args:
                number[]
            ): void {
              const isVerifyPlusCapture =
                this.canvas ===
                captureCanvas &&
                image ===
                video;

              if (
                isVerifyPlusCapture
              ) {
                diagnostics
                  .injectedFrames +=
                  1;

                Reflect.apply(
                  nativeDrawImage,
                  this,
                  [
                    qrCanvas,
                    ...args,
                  ],
                );

                return;
              }

              Reflect.apply(
                nativeDrawImage,
                this,
                [
                  image,
                  ...args,
                ],
              );
            },
        },
      );
    },
    {
      imageDataUrl:
      qrPngDataUrl,

      diagnosticsProperty:
      DIAGNOSTICS_PROPERTY,
    },
  );
}

/**
 * Returns current diagnostics from the deterministic Verify+ camera.
 *
 * @param page Playwright page containing the installed camera
 * @returns current camera diagnostic counters
 */
export async function getQrCameraDiagnostics(
  page: Page,
): Promise<VerifyPlusQrCameraDiagnostics> {
  return page.evaluate(
    diagnosticsProperty => {
      const testWindow =
        window as typeof window & {
          [key: string]:
            unknown;
        };

      return (
        testWindow[
          diagnosticsProperty
          ] as
          VerifyPlusQrCameraDiagnostics |
          undefined
      ) ?? {
        getUserMediaCalls:
          0,

        srcObjectSets:
          0,

        playCalls:
          0,

        pauseCalls:
          0,

        videoWidthReads:
          0,

        videoHeightReads:
          0,

        injectedFrames:
          0,
      };
    },
    DIAGNOSTICS_PROPERTY,
  );
}

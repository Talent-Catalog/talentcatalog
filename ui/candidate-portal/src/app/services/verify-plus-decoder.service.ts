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
import {Injectable} from '@angular/core';
import {
  prepareZXingModule,
  readBarcodes,
  ReaderOptions,
  ZXingReaderModule
} from 'zxing-wasm/reader';

/**
 * Service for decoding QR codes using the zxing-wasm library.
 *
 * This service initialises the zxing-wasm module and provides a method to decode QR codes from
 * image data.
 * It is designed to be used in conjunction with camera capture components to process frames and
 * extract QR code information.
 *
 * @author sadatmalik
 */
@Injectable({
  providedIn: 'root'
})
export class VerifyPlusDecoderService {
  private moduleReady?: Promise<ZXingReaderModule>;

  private readonly options: ReaderOptions = {
    formats: ['QRCode'],
    tryHarder: true,
    tryRotate: true,
    tryInvert: true,
    tryDownscale: true,
    maxNumberOfSymbols: 1
  };

  private init(): Promise<ZXingReaderModule> {
    if (!this.moduleReady) {
      this.moduleReady = prepareZXingModule({
        overrides: {
          locateFile: (path: string, prefix: string) =>
            path.endsWith('.wasm') ? 'assets/wasm/zxing_reader.wasm' : prefix + path
        },
        fireImmediately: true
      }) as Promise<ZXingReaderModule>;
    }

    return this.moduleReady;
  }

  /**
   * Decodes a single QR code from a captured camera frame.
   *
   * Returns a Promise (rather than the Observable used by the portal's
   * HttpClient-backed services) on purpose: zxing-wasm exposes a Promise-based
   * API and this is a one-shot, single-value decode with no stream semantics, so
   * a Promise is the natural fit (cf. the AWS-wrapping S3HelperService).
   *
   * The WASM module is lazily initialised and cached on first use via init(),
   * so the one-time download/instantiation cost is paid once and shared.
   *
   * @param imageData a single frame captured from the camera preview
   * @returns the decoded QR text, or `null` if no QR code was found in the frame
   */
  async decode(imageData: ImageData): Promise<string | null> {
    await this.init();
    const results = await readBarcodes(imageData, this.options);
    const hit = results.find(result => Boolean(result.text));
    return hit?.text ?? null;
  }
}

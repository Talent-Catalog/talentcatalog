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
import {TestBed} from '@angular/core/testing';
import {VerifyPlusDecoderService} from './verify-plus-decoder.service';

describe('VerifyPlusDecoderService', () => {
  let service: VerifyPlusDecoderService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [VerifyPlusDecoderService]
    });

    service = TestBed.inject(VerifyPlusDecoderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should have QR-code decoding options configured', () => {
    expect((service as any).options).toEqual({
      formats: ['QRCode'],
      tryHarder: true,
      tryRotate: true,
      tryInvert: true,
      tryDownscale: true,
      maxNumberOfSymbols: 1
    });
  });

  it('should decode the UNHCR sample fixture image', async () => {
    const imageData = await loadFixtureImageData(
      '/docs/verify-plus/unhcr-sample.png'
    );

    const decoded = await service.decode(imageData);

    expect(decoded).toBeTruthy();
    expect(decoded!.length).toBeGreaterThan(1000);
  });

  it('should return null when the image contains no QR code', async () => {
    const blankImage = createBlankImageData(100, 100);

    const decoded = await service.decode(blankImage);

    expect(decoded).toBeNull();
  });

  it('should cache and reuse the initialized ZXing module', async () => {
    const blankImage = createBlankImageData(50, 50);

    await service.decode(blankImage);

    const firstModulePromise =
      (service as any).moduleReady;

    expect(firstModulePromise).toBeTruthy();

    await service.decode(blankImage);

    const secondModulePromise =
      (service as any).moduleReady;

    expect(secondModulePromise).toBe(
      firstModulePromise
    );
  });

  it('should return an existing cached module from init', () => {
    const cachedModule = Promise.resolve({});

    (service as any).moduleReady = cachedModule;

    const result = (service as any).init();

    expect(result).toBe(cachedModule);
  });

  it('should decode the UNHCR sample fixture image', async () => {
    const response = await fetch('/base/docs/verify-plus/unhcr-sample.png');
    if (!response.ok) {
      pending('UNHCR fixture image is not available in Karma static files.');
      return;
    }

    const blob = await response.blob();
    const image = await loadImage(URL.createObjectURL(blob));

    const canvas = document.createElement('canvas');
    canvas.width = image.naturalWidth;
    canvas.height = image.naturalHeight;

    const context = canvas.getContext('2d');
    if (!context) {
      fail('Unable to create 2D canvas context for fixture decode test.');
      return;
    }

    context.drawImage(image, 0, 0);
    const imageData = context.getImageData(0, 0, canvas.width, canvas.height);
    const decoded = await service.decode(imageData);

    expect(decoded).toBeTruthy();
    expect(decoded!.length).toBeGreaterThan(1000);
  });

});

function loadImage(url: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const image = new Image();
    image.onload = () => resolve(image);
    image.onerror = reject;
    image.src = url;
  });
}

async function loadFixtureImageData(
  path: string
): Promise<ImageData> {
  const image = await loadImage(path);

  const canvas = document.createElement('canvas');
  canvas.width = image.naturalWidth;
  canvas.height = image.naturalHeight;

  const context = canvas.getContext('2d');

  if (!context) {
    throw new Error(
      'Unable to create 2D canvas context for QR fixture.'
    );
  }

  context.drawImage(image, 0, 0);

  return context.getImageData(
    0,
    0,
    canvas.width,
    canvas.height
  );
}

function createBlankImageData(
  width: number,
  height: number
): ImageData {
  const pixels = new Uint8ClampedArray(
    width * height * 4
  );

  // Create a fully opaque white image.
  for (let index = 0; index < pixels.length; index += 4) {
    pixels[index] = 255;
    pixels[index + 1] = 255;
    pixels[index + 2] = 255;
    pixels[index + 3] = 255;
  }

  return new ImageData(
    pixels,
    width,
    height
  );
}


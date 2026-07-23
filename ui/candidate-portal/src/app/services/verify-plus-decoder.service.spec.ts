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

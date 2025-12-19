/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {EncodedQrImage} from "./qr";

export class QrImage implements EncodedQrImage {
  base64Encoding: string;

  constructor(base64Encoding: string) {
    this.base64Encoding = base64Encoding;
  }

  isValidBase64(): boolean {
    const base64Regex = /^(?:[A-Z0-9+/]{4})*(?:[A-Z0-9+/]{2}==|[A-Z0-9+/]{3}=)?$/i;
    return base64Regex.test(this.base64Encoding);
  }
}

describe('QrImage', () => {
  it('should create an instance of QrImage with the given base64 encoding', () => {
    const base64Encoding = 'dGVzdA==';
    const qrImage = new QrImage(base64Encoding);

    expect(qrImage).toBeTruthy();
    expect(qrImage.base64Encoding).toBe(base64Encoding);
  });

  it('should validate a correct base64 encoding', () => {
    const base64Encoding = 'dGVzdA==';
    const qrImage = new QrImage(base64Encoding);

    expect(qrImage.isValidBase64()).toBe(true);
  });

  it('should invalidate an incorrect base64 encoding', () => {
    const base64Encoding = 'invalid-base64';
    const qrImage = new QrImage(base64Encoding);

    expect(qrImage.isValidBase64()).toBe(false);
  });

  it('should invalidate an undefined base64 encoding', () => {
    const base64Encoding = undefined;
    const qrImage = new QrImage(base64Encoding);

    expect(qrImage.isValidBase64()).toBe(false);
  });
});

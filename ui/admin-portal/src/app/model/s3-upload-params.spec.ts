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

import {S3UploadParams} from "./s3-upload-params";

describe('S3UploadParams Interface Tests', () => {

  it('should create an instance of S3UploadParams', () => {
    const params: S3UploadParams = {
      objectKey: 'uploads/',
      key: 'file.txt',
      policy: 'sample-policy',
      signature: 'sample-signature'
    };

    expect(params).toBeDefined();
    expect(params.objectKey).toBe('uploads/');
    expect(params.key).toBe('file.txt');
    expect(params.policy).toBe('sample-policy');
    expect(params.signature).toBe('sample-signature');
  });

  it('should handle missing optional properties', () => {
    const params: S3UploadParams = {
      objectKey: 'uploads/',
      key: 'file.txt',
      policy: 'sample-policy',
      signature: 'sample-signature'
    };

    // Simulate optional properties being undefined
    delete params.policy;
    delete params.signature;

    expect(params).toBeDefined();
    expect(params.objectKey).toBe('uploads/');
    expect(params.key).toBe('file.txt');
    expect(params.policy).toBeUndefined();
    expect(params.signature).toBeUndefined();
  });

});


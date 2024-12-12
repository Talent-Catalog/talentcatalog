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

import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {S3HelperService} from './s3-helper.service';
import {S3UploadParams} from '../model/s3-upload-params';
import {environment} from '../../environments/environment';

describe('S3HelperService', () => {
  let service: S3HelperService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [S3HelperService]
    });

    service = TestBed.inject(S3HelperService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('#getUploadPolicy', () => {
    it('should return an Observable S3UploadParams ', () => {
      const dummyParams: S3UploadParams = {
        key: 'testKey',
        policy: 'testPolicy',
        signature: 'testSignature',
        objectKey: 'testObjectKey'
      };

      service.getUploadPolicy('testFolder').subscribe(params => {
        expect(params).toEqual(dummyParams);
      });

      const req = httpMock.expectOne(`${environment.systemApiUrl}/upload/policy/testFolder`);
      expect(req.request.method).toBe('GET');
      req.flush(dummyParams);
    });
  });

  describe('#getContentType', () => {
    it('should return correct content type for known file extensions', () => {
      expect(service['getContentType']('test.zip')).toBe('application/zip');
      expect(service['getContentType']('test.jpg')).toBe('image/jpeg');
      expect(service['getContentType']('test.png')).toBe('image/png');
    });

    it('should return default content type for unknown file extensions', () => {
      expect(service['getContentType']('test.unknown')).toBe('application/octet-stream');
    });

    it('should return default content type for files without extension', () => {
      expect(service['getContentType']('test')).toBe('application/octet-stream');
    });
  });
});

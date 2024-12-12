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
import {LinkPreviewService, BuildLinkPreviewRequest} from './link-preview.service';
import {LinkPreview} from '../model/link-preview';
import {environment} from '../../environments/environment';

describe('LinkPreviewService', () => {
  let service: LinkPreviewService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LinkPreviewService]
    });
    service = TestBed.inject(LinkPreviewService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should delete a link preview', () => {
    const dummyId = 123;
    service.delete(dummyId).subscribe((response) => {
      expect(response).toBeTrue();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/link-preview/${dummyId}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(true);
  });

  it('should build a link preview', () => {
    const request: BuildLinkPreviewRequest = { url: 'https://example.com' };
    const dummyLinkPreview: LinkPreview = {
      url: 'https://example.com',
      title: 'Example Title',
      description: 'Example Description',
      imageUrl: 'https://example.com/image.jpg',
      domain:'Example Domain',
      blocked: false,
      faviconUrl: 'https://example.com/favUrl',
      id:1
    };

    service.buildLinkPreview(request).subscribe((response) => {
      expect(response).toEqual(dummyLinkPreview);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/link-preview/build-link-preview`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(dummyLinkPreview);
  });
});

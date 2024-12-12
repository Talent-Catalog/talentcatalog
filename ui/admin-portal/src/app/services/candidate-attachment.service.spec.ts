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
import {
  CandidateAttachmentService,
  ListByUploadTypeRequest,
  SearchCandidateAttachmentsRequest,
  UpdateCandidateAttachmentRequest
} from './candidate-attachment.service';
import {environment} from '../../environments/environment';
import {CandidateAttachment, CandidateAttachmentRequest} from '../model/candidate-attachment';

describe('CandidateAttachmentService', () => {
  let service: CandidateAttachmentService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/candidate-attachment';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateAttachmentService]
    });
    service = TestBed.inject(CandidateAttachmentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should search attachments', () => {
    const request: SearchCandidateAttachmentsRequest = { candidateId: 1, cvOnly: true };
    const mockAttachments: CandidateAttachment[] = [{ id: 1, name: 'CV', location: '', fileType: 'pdf', url: '', cv: true } as CandidateAttachment];

    service.search(request).subscribe((attachments) => {
      expect(attachments.length).toBe(1);
      expect(attachments).toEqual(mockAttachments);
    });

    const req = httpMock.expectOne(`${apiUrl}/search`);
    expect(req.request.method).toBe('POST');
    req.flush(mockAttachments);
  });

  it('should create an attachment', () => {
    const details: CandidateAttachmentRequest = { candidateId: 1, name: 'CV', fileType: 'pdf'} as CandidateAttachmentRequest;
    const mockAttachment: CandidateAttachment = { id: 1, name: 'CV', location: '', fileType: 'pdf', url: '', cv: true } as CandidateAttachment;

    service.createAttachment(details).subscribe((attachment) => {
      expect(attachment).toEqual(mockAttachment);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    req.flush(mockAttachment);
  });

  it('should delete an attachment', () => {
    const mockAttachment: CandidateAttachment = { id: 1, name: 'CV', location: '', fileType: 'pdf', url: '', cv: true } as CandidateAttachment;

    service.deleteAttachment(1).subscribe((attachment) => {
      expect(attachment).toEqual(mockAttachment);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(mockAttachment);
  });

  it('should update an attachment', () => {
    const request: UpdateCandidateAttachmentRequest = { id: 1, name: 'Updated CV' };
    const mockAttachment: CandidateAttachment = { id: 1, name: 'Updated CV', location: '', fileType: 'pdf', url: '', cv: true } as CandidateAttachment;

    service.updateAttachment(1, request).subscribe((attachment) => {
      expect(attachment).toEqual(mockAttachment);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockAttachment);
  });

  it('should list attachments by type', () => {
    const request: ListByUploadTypeRequest = { candidateId: 1, uploadType: 'pdf' };
    const mockAttachments: CandidateAttachment[] = [{ id: 1, name: 'CV', location: '', fileType: 'pdf', url: '', cv: true } as CandidateAttachment];

    service.listByType(request).subscribe((attachments) => {
      expect(attachments.length).toBe(1);
      expect(attachments).toEqual(mockAttachments);
    });

    const req = httpMock.expectOne(`${apiUrl}/list-by-type`);
    expect(req.request.method).toBe('POST');
    req.flush(mockAttachments);
  });
});

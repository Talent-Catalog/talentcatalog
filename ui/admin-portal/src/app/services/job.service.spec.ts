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
import {JobService} from './job.service';
import {Job, JobDocType, SearchJobRequest, UpdateJobRequest} from '../model/job';
import {SearchResults} from '../model/search-results';
import {UpdateLinkRequest} from '../components/util/input/input-link/input-link.component';
import {JobChatUserInfo} from '../model/chat';
import {environment} from "../../environments/environment";
import {MockJob} from "../MockData/MockJob";

describe('JobService', () => {
  let service: JobService;
  let httpTestingController: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/job`;
  const mockJob: Job = MockJob;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [JobService]
    });

    service = TestBed.inject(JobService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should create a job', () => {
    const request: UpdateJobRequest = { roleName:'Admin' };
    service.create(request).subscribe((response) => {
      expect(response).toEqual(mockJob);
    });

    const req = httpTestingController.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockJob);
  });

  it('should create a suggested search', () => {
    const id = 1;
    const suffix = 'search-suffix';

    service.createSuggestedSearch(id, suffix).subscribe((response) => {
      expect(response).toEqual(mockJob);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/${id}/create-search`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(suffix);
    req.flush(mockJob);
  });

  it('should get a job by ID', () => {
    const id = 1;

    service.get(id).subscribe((response) => {
      expect(response).toEqual(mockJob);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/${id}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockJob);
  });

  it('should publish a job', () => {
    const id = 1;

    service.publishJob(id).subscribe((response) => {
      expect(response).toEqual(mockJob);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/${id}/publish`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockJob);
  });

  it('should remove a suggested search', () => {
    const id = 1;
    const savedSearchId = 2;

    service.removeSuggestedSearch(id, savedSearchId).subscribe((response) => {
      expect(response).toEqual(mockJob);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/${id}/remove-search`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(savedSearchId);
    req.flush(mockJob);
  });

  it('should check unread chats', () => {
    const request: SearchJobRequest = { starred:true };
    const mockChatInfo: JobChatUserInfo = { numberUnreadChats:3 };

    service.checkUnreadChats(request).subscribe((response) => {
      expect(response).toEqual(mockChatInfo);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/check-unread-chats`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockChatInfo);
  });

  it('should search jobs with paging', () => {
    const request: SearchJobRequest = { starred:true };
    const mockResponse: SearchResults<Job> = {
      content: [
        mockJob,
        mockJob
      ],
      totalPages: 2
    } as SearchResults<Job>;

    service.searchPaged(request).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/search-paged`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should search jobs', () => {
    const request: SearchJobRequest = { starred:false };
    const mockJobs: Job[] = [
      mockJob,mockJob
    ];

    service.search(request).subscribe((response) => {
      expect(response).toEqual(mockJobs);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/search`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockJobs);
  });

  it('should update a job', () => {
    const id = 1;
    const request: UpdateJobRequest = { roleName:'Admin' };

    service.update(id, request).subscribe((response) => {
      expect(response).toEqual(mockJob);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/${id}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(mockJob);
  });

  it('should update intake data', () => {
    const id = 1;
    const formData: Object = { };

    service.updateIntakeData(id, formData).subscribe();

    const req = httpTestingController.expectOne(`${apiUrl}/${id}/intake`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(formData);
    req.flush({});
  });

  it('should update job link', () => {
    const id = 1;
    const docType: JobDocType = 'interview';
    const updateLinkRequest: UpdateLinkRequest = { };

    service.updateJobLink(id, docType, updateLinkRequest).subscribe((response) => {
      expect(response).toEqual(mockJob);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/${id}/${docType}-link`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateLinkRequest);
    req.flush(mockJob);
  });

  it('should update starred status', () => {
    const id = 1;
    const starred = true;

    service.updateStarred(id, starred).subscribe((response) => {
      expect(response).toEqual(mockJob);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/${id}/starred`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(starred);
    req.flush(mockJob);
  });

  it('should update job summary', () => {
    const id = 1;
    const summary = 'Updated Job Summary';

    service.updateSummary(id, summary).subscribe((response) => {
      expect(response).toEqual(mockJob);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/${id}/summary`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(summary);
    req.flush(mockJob);
  });

  it('should upload job document', () => {
    const id = 1;
    const docType: JobDocType = 'interview';
    const formData = new FormData();

    service.uploadJobDoc(id, docType, formData).subscribe((response) => {
      expect(response).toEqual(mockJob);
    });

    const req = httpTestingController.expectOne(`${apiUrl}/${id}/upload/${docType}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(formData);
    req.flush(mockJob);
  });
});

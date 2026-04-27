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
import {CandidateOpportunityService} from './candidate-opportunity.service';
import {environment} from '../../environments/environment';
import {
  CandidateOpportunity,
  CandidateOpportunityStage,
  SearchOpportunityRequest
} from '../model/candidate-opportunity';
import {SearchResults} from '../model/search-results';
import {CandidateOpportunityParams} from '../model/candidate';
import {JobChatUserInfo} from '../model/chat';

describe('CandidateOpportunityService', () => {
  let service: CandidateOpportunityService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/opp';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateOpportunityService]
    });
    service = TestBed.inject(CandidateOpportunityService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should retrieve a candidate opportunity by id', () => {
    const mockOpportunity: CandidateOpportunity = {
      closed: false, won: false,
      id: 234,
      name: 'Fred(12345)-Test opp',
      jobOpp: {
        id: 123,
        name: 'Test job'
      },
      stage: CandidateOpportunityStage.prospect,
      nextStep: 'Come on get this guy hired',
      employerFeedback: 'English is not great',
      candidate: { /* mock ShortCandidate */ } as any
    };

    service.get(234).subscribe(opportunity => {
      expect(opportunity).toEqual(mockOpportunity);
    });

    const req = httpMock.expectOne(`${apiUrl}/234`);
    expect(req.request.method).toBe('GET');
    req.flush(mockOpportunity);
  });

  it('should check for unread chats', () => {
    const request: SearchOpportunityRequest = { keyword: 'test' };
    const mockUserInfo: JobChatUserInfo = { numberUnreadChats: 1, lastReadPostId: 5 };

    service.checkUnreadChats(request).subscribe(userInfo => {
      expect(userInfo).toEqual(mockUserInfo);
    });

    const req = httpMock.expectOne(`${apiUrl}/check-unread-chats`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockUserInfo);
  });

  it('should search paged opportunities', () => {
    const request: SearchOpportunityRequest = { keyword: 'test' };
    const mockSearchResults: SearchResults<CandidateOpportunity> = {
      number: 1,
      size: 1,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
      content: [{ /* mock CandidateOpportunity */ } as any]
    };

    service.searchPaged(request).subscribe(searchResults => {
      expect(searchResults).toEqual(mockSearchResults);
    });

    const req = httpMock.expectOne(`${apiUrl}/search-paged`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockSearchResults);
  });

  it('should update a candidate opportunity', () => {
    const mockOpportunity: CandidateOpportunity = {
      closed: false, won: false,
      id: 234,
      name: 'Fred(12345)-Test opp',
      jobOpp: {
        id: 123,
        name: 'Test job'
      },
      stage: CandidateOpportunityStage.prospect,
      nextStep: 'Come on get this guy hired',
      employerFeedback: 'English is not great',
      candidate: { /* mock ShortCandidate */ } as any
    };
    const params: CandidateOpportunityParams = { /* mock params */ };

    service.updateCandidateOpportunity(234, params).subscribe(opportunity => {
      expect(opportunity).toEqual(mockOpportunity);
    });

    const req = httpMock.expectOne(`${apiUrl}/234`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(params);
    req.flush(mockOpportunity);
  });

  it('should upload an offer', () => {
    const mockOpportunity: CandidateOpportunity = {
      closed: false, won: false,
      id: 234,
      name: 'Fred(12345)-Test opp',
      jobOpp: {
        id: 123,
        name: 'Test job'
      },
      stage: CandidateOpportunityStage.prospect,
      nextStep: 'Come on get this guy hired',
      employerFeedback: 'English is not great',
      candidate: { /* mock ShortCandidate */ } as any
    };
    const formData = new FormData();

    service.uploadOffer(234, formData).subscribe(opportunity => {
      expect(opportunity).toEqual(mockOpportunity);
    });

    const req = httpMock.expectOne(`${apiUrl}/234/upload-offer`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(formData);
    req.flush(mockOpportunity);
  });
});

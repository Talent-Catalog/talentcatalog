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
import {SlackService} from './slack.service';
import {environment} from '../../environments/environment';
import {PostJobToSlackRequest, PostJobToSlackResponse} from '../model/base';

describe('SlackService', () => {
  let service: SlackService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/slack';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SlackService]
    });
    service = TestBed.inject(SlackService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should post a job to Slack', () => {
    const mockRequest: PostJobToSlackRequest = { jobName: 'Software Engineer'} as PostJobToSlackRequest;
    const mockResponse: PostJobToSlackResponse = { slackChannelUrl:'https://example.slack.com'};

    service.postJob(mockRequest).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/post-job`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should post a job to Slack using job ID', () => {
    const mockId = 1;
    const mockTcJobLink = 'https://example.com/job-link';
    const mockResponse: PostJobToSlackResponse = { slackChannelUrl:'https://example.slack.com'};

    service.postJobFromId(mockId, mockTcJobLink).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${mockId}/post-job`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockTcJobLink);
    req.flush(mockResponse);
  });
});

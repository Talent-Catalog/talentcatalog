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
import {ChatPostService} from './chat-post.service';
import {ChatPost} from '../model/chat';
import {UrlDto} from '../model/url-dto';
import {environment} from '../../environments/environment';

describe('ChatPostService', () => {
  let service: ChatPostService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ChatPostService]
    });
    service = TestBed.inject(ChatPostService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('#listPosts', () => {
    it('should return an Observable ChatPost[] ', () => {
      const mockChatPosts: ChatPost[] = [
        { id: 1, content: 'Test Post 1'} as ChatPost,
        { id: 2, content: 'Test Post 2' } as ChatPost
      ];

      service.listPosts(1).subscribe(posts => {
        expect(posts.length).toBe(2);
        expect(posts).toEqual(mockChatPosts);
      });

      const req = httpMock.expectOne(`${environment.chatApiUrl}/chat-post/1/list`);
      expect(req.request.method).toBe('GET');
      req.flush(mockChatPosts);
    });
  });

  describe('#uploadFile', () => {
    it('should return an Observable UrlDto ', () => {
      const mockUrlDto: UrlDto = { url: 'http://localhost/test-file' };
      const formData = new FormData();

      service.uploadFile(1, formData).subscribe(urlDto => {
        expect(urlDto).toEqual(mockUrlDto);
      });

      const req = httpMock.expectOne(`${environment.chatApiUrl}/chat-post/1/upload`);
      expect(req.request.method).toBe('POST');
      req.flush(mockUrlDto);
    });
  });
});

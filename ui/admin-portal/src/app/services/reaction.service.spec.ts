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
import {ReactionService, AddReactionRequest} from './reaction.service';
import {Reaction} from '../model/reaction';
import {environment} from '../../environments/environment';

describe('ReactionService', () => {
  let service: ReactionService;
  let httpMock: HttpTestingController;
  const displayUser = {
    id:1,
    displayName: 'Test User'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ReactionService]
    });
    service = TestBed.inject(ReactionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });


  describe('addReaction', () => {
    it('should make a POST request and return an array of reactions', () => {
      const mockReactions: Reaction[] = [
        { id: 1, emoji: '👍', users : [displayUser] },
        { id: 2, emoji: '😊', users : [displayUser] },
      ];

      const requestPayload: AddReactionRequest = { emoji: '👍' };
      const chatPostId = 1;

      service.addReaction(chatPostId, requestPayload).subscribe(reactions => {
        expect(reactions).toEqual(mockReactions);
      });

      const req = httpMock.expectOne(`${environment.chatApiUrl}/reaction/${chatPostId}/add-reaction`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(requestPayload);
      req.flush(mockReactions);
    });
  });


  describe('modifyReaction', () => {
    it('should make a PUT request and return an array of reactions', () => {
      const mockReactions: Reaction[] = [
        { id: 1, emoji: '👍', users : [displayUser] },
        { id: 2, emoji: '😊', users : [displayUser] },
      ];
      const postId = 1;
      const reactionId = 1;

      service.modifyReaction(postId, reactionId).subscribe(reactions => {
        expect(reactions).toEqual(mockReactions);
      });

      const req = httpMock.expectOne(`${environment.chatApiUrl}/reaction/${postId}/modify-reaction/${reactionId}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toBeNull();
      req.flush(mockReactions);
    });
  });

});

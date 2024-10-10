/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import {ReactionService, AddReactionRequest} from './reaction.service';
import {RxStompService} from "./rx-stomp.service";
import {of} from "rxjs";

describe('ReactionService', () => {
  let service: ReactionService;
  let rxStompServiceSpy: jasmine.SpyObj<RxStompService>;

  const displayUser = {
    id:1,
    displayName: 'Test User'
  };

  beforeEach(() => {
    const spy = jasmine.createSpyObj('RxStompService', ['watch', 'publish']);

    TestBed.configureTestingModule({
      providers: [
        ReactionService,
        { provide: RxStompService, useValue: spy }
      ]
    });
    service = TestBed.inject(ReactionService);
    rxStompServiceSpy = TestBed.inject(RxStompService) as jasmine.SpyObj<RxStompService>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('addReaction', () => {
    it('should publish a new reaction via WebSocket', () => {
      const chatPostId = 1;
      const requestPayload: AddReactionRequest = { emoji: '👍' };
      const expectedDestination = `/app/reaction/${chatPostId}/add`;

      service.addReaction(chatPostId, requestPayload);

      expect(rxStompServiceSpy.publish).toHaveBeenCalledWith({
        destination: expectedDestination,
        body: JSON.stringify(requestPayload)
      });
    });

  });

  describe('modifyReaction', () => {
    it('should publish a modification of a reaction via WebSocket', () => {
      const chatPostId = 1;
      const reactionId = 1;
      const expectedDestination = `/app/reaction/${chatPostId}/modify/${reactionId}`;

      service.modifyReaction(chatPostId, reactionId);

      expect(rxStompServiceSpy.publish).toHaveBeenCalledWith({
        destination: expectedDestination
      });
    });
  });

  describe('subscribeToReactions', () => {
    it('should subscribe to the correct WebSocket topic for reaction updates', () => {
      const chatPostId = 1;
      const expectedTopic = `/topic/reaction/${chatPostId}`;
      const mockReactionMessage = JSON.stringify([
        { id: 1, emoji: '👍', users: [displayUser] },
        { id: 2, emoji: '😊', users: [displayUser] }
      ]);

      // Create a mock stomp message
      const mockMessage = {
        body: mockReactionMessage,
        ack: () => {},
        nack: () => {},
        command: '',
        headers: {},
        binaryBody: new Uint8Array(),
        isBinaryBody: false
      };

      // Simulate an observable returned by the watch method
      rxStompServiceSpy.watch.and.returnValue(of(mockMessage));

      service.subscribeToReactions(chatPostId).subscribe((reactions) => {
        expect(reactions.length).toBe(2);
        expect(reactions[0].emoji).toBe('👍');
        expect(reactions[1].emoji).toBe('😊');
      });

      expect(rxStompServiceSpy.watch).toHaveBeenCalledWith(expectedTopic);
    });
  });

});

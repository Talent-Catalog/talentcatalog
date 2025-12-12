/*
 * Copyright (c) 2025 Talent Catalog.
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
import {NgZone} from '@angular/core';
import {CrossTabSyncService} from './cross-tab-sync.service';

describe('CrossTabSyncService', () => {
  let service: CrossTabSyncService;
  let zone: NgZone;
  let mockChannel: MockBroadcastChannel;

  beforeEach(() => {
    // Mock BroadcastChannel so tests do not depend on the browser implementation
    mockChannel = new MockBroadcastChannel('tc-candidate-intake-updates');

    (window as any).BroadcastChannel = function (this: any, name: string) {
      return mockChannel;
    } as any;

    TestBed.configureTestingModule({
      providers: [CrossTabSyncService]
    });

    service = TestBed.inject(CrossTabSyncService);
    zone = TestBed.inject(NgZone);
  });

// Clean up the global mock to avoid affecting other tests
  afterEach(() => {
    delete (window as any).BroadcastChannel;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('#broadcastCandidateUpdated', () => {
    it('should post a message with candidate id and timestamp', () => {
      const beforeTime = Date.now();
      service.broadcastCandidateUpdated(42);
      const afterTime = Date.now();

      expect(mockChannel.postMessage).toHaveBeenCalledTimes(1);

      const payload = mockChannel.postMessage.calls.mostRecent().args[0];
      expect(payload.id).toBe(42);
      expect(typeof payload.ts).toBe('number');
      expect(payload.ts).toBeGreaterThanOrEqual(beforeTime);
      expect(payload.ts).toBeLessThanOrEqual(afterTime);
    });
  });

  describe('incoming BroadcastChannel messages', () => {
    it('should emit candidateUpdated$ when a message is received', (done) => {
      const payload = { id: 99, ts: Date.now() };

      service.candidateUpdated$.subscribe(event => {
        expect(event).toEqual(payload);
        done();
      });

      mockChannel.emit(payload);
    });

    it('should run message handling inside NgZone', (done) => {
      const runSpy = spyOn(zone, 'run').and.callThrough();
      const payload = { id: 1, ts: Date.now() };


      service.candidateUpdated$.subscribe(() => {
        expect(runSpy).toHaveBeenCalledTimes(1);
        done();
      });

      mockChannel.emit(payload);
    });
    
    it('should handle multiple messages', (done) => {
      const messages: any[] = [];

      service.candidateUpdated$.subscribe(event => {
        messages.push(event);
        if (messages.length === 2) {
          expect(messages).toEqual([
            { id: 1, ts: 100 },
            { id: 2, ts: 200 }
          ]);
          done();
        }
      });

      mockChannel.emit({ id: 1, ts: 100 });
      mockChannel.emit({ id: 2, ts: 200 });
    });
  });
});

/* Simple mock for the BroadcastChannel API */
class MockBroadcastChannel {
  name: string;
  onmessage: ((event: MessageEvent) => void) | null = null;
  postMessage = jasmine.createSpy('postMessage');

  constructor(name: string) {
    this.name = name;
  }

  emit(data: any) {
    if (this.onmessage) {
      this.onmessage({ data } as MessageEvent);
    }
  }

  close() {}
}

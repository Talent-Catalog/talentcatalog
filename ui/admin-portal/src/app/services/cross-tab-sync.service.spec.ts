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

/**
 * Test suite for CrossTabSyncService
 *
 * This service uses the BroadcastChannel API to synchronize candidate updates across browser tabs.
 * Since BroadcastChannel may not be available in test environments, we mock it to test the service behavior.
 */
describe('CrossTabSyncService', () => {
  let service: CrossTabSyncService;
  let zone: NgZone;
  let mockChannel: MockBroadcastChannel;
  let originalBroadcastChannel: any;

  beforeEach(() => {
    // Save the original BroadcastChannel implementation (if it exists) so we can restore it after tests.
    // This prevents our mock from affecting other test suites.
    originalBroadcastChannel = (window as any).BroadcastChannel;

    // Create a mock BroadcastChannel that we control
    mockChannel = new MockBroadcastChannel('tc-candidate-intake-updates');

    // Replace the global BroadcastChannel constructor with our mock.
    // When the service creates a new BroadcastChannel, it will get our mock instead.
    (window as any).BroadcastChannel = jasmine.createSpy('BroadcastChannel')
    .and.returnValue(mockChannel);

    // Set up the Angular testing module with our service
    TestBed.configureTestingModule({
      providers: [CrossTabSyncService]
    });

    // Create instances of the service and NgZone for testing
    service = TestBed.inject(CrossTabSyncService);
    zone = TestBed.inject(NgZone);
  });

  afterEach(() => {
    // Restore the original BroadcastChannel to prevent test pollution.
    // If BroadcastChannel didn't exist originally, delete our mock entirely.
    if (originalBroadcastChannel !== undefined) {
      (window as any).BroadcastChannel = originalBroadcastChannel;
    } else {
      delete (window as any).BroadcastChannel;
    }
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('#broadcastCandidateUpdated', () => {
    it('should post a message with candidate id and timestamp', () => {
      // Capture timestamps before and after the broadcast to verify the timestamp is reasonable
      const beforeTime = Date.now();
      service.broadcastCandidateUpdated(42);
      const afterTime = Date.now();

      // Verify that postMessage was called exactly once
      expect(mockChannel.postMessage).toHaveBeenCalledTimes(1);

      // Extract the payload that was sent
      const payload = mockChannel.postMessage.calls.mostRecent().args[0];

      // Verify the payload structure
      expect(payload.id).toBe(42);
      expect(typeof payload.ts).toBe('number');

      // Verify the timestamp is within the expected range
      expect(payload.ts).toBeGreaterThanOrEqual(beforeTime);
      expect(payload.ts).toBeLessThanOrEqual(afterTime);
    });
  });

  describe('incoming BroadcastChannel messages', () => {
    it('should emit candidateUpdated$ when a message is received', (done) => {
      const payload = { id: 99, ts: Date.now() };

      // Subscribe to the service's observable to verify it emits the message
      service.candidateUpdated$.subscribe(event => {
        expect(event).toEqual(payload);
        done(); // Signal that the async test is complete
      });

      // Simulate a message from another tab
      mockChannel.emit(payload);
    });

    it('should run message handling inside NgZone', (done) => {
      // Spy on zone.run to verify it's called when messages arrive.
      // This ensures Angular change detection is triggered properly.
      const runSpy = spyOn(zone, 'run').and.callThrough();
      const payload = { id: 1, ts: Date.now() };

      service.candidateUpdated$.subscribe(() => {
        // Verify zone.run was called exactly once
        expect(runSpy).toHaveBeenCalledTimes(1);
        done();
      });

      mockChannel.emit(payload);
    });

    it('should handle multiple messages', (done) => {
      const messages: any[] = [];

      // Subscribe and collect all messages
      service.candidateUpdated$.subscribe(event => {
        messages.push(event);

        // Once we've received both messages, verify they match what we sent
        if (messages.length === 2) {
          expect(messages).toEqual([
            { id: 1, ts: 100 },
            { id: 2, ts: 200 }
          ]);
          done();
        }
      });

      // Simulate two separate messages from other tabs
      mockChannel.emit({ id: 1, ts: 100 });
      mockChannel.emit({ id: 2, ts: 200 });
    });
  });
});

/**
 * Mock implementation of the BroadcastChannel API for testing.
 *
 * This mock allows us to:
 * 1. Track calls to postMessage using Jasmine spies
 * 2. Simulate incoming messages from other tabs using the emit() method
 * 3. Test the service without requiring actual browser tab communication
 */
class MockBroadcastChannel {
  name: string;
  onmessage: ((event: MessageEvent) => void) | null = null;
  postMessage = jasmine.createSpy('postMessage');

  constructor(name: string) {
    this.name = name;
  }

  /**
   * Simulates receiving a message from another tab.
   * Triggers the onmessage handler if one is registered.
   */
  emit(data: any) {
    if (this.onmessage) {
      this.onmessage({ data } as MessageEvent);
    }
  }

  close() {}
}

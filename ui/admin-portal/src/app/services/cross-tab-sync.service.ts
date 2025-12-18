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

import {Injectable, NgZone} from '@angular/core';
import {Subject} from 'rxjs';


/**
 * Channel name used to communicate candidate intake updates
 * between different browser tabs or windows.
 */
const CHANNEL = 'tc-candidate-intake-updates';

@Injectable({ providedIn: 'root' })
export class CrossTabSyncService {
  /**
   * BroadcastChannel used to send and receive messages
   * across browser tabs.
   */
  private bc = new BroadcastChannel(CHANNEL);
  /**
   * Internal subject that emits when a candidate intake
   * is updated in any tab.
   */
  private updates$ = new Subject<{ id: number; ts: number }>();

  /**
   * Public observable that components can subscribe to
   * in order to react to candidate intake updates coming
   * from other tabs.
   */
  candidateUpdated$ = this.updates$.asObservable();

  constructor(private zone: NgZone) {
    /**
     * Listen for messages coming from other tabs.
     * NgZone.run is used to make sure Angular change
     * detection is triggered when a message is received.
     */
    this.bc.onmessage = (ev: MessageEvent) =>
      this.zone.run(() => this.updates$.next(ev.data));
  }

  broadcastCandidateUpdated(id: number) {
    /**
     * Broadcasts a candidate intake update to all open tabs.
     * This allows other tabs to refresh or sync their state.
     */
    this.bc.postMessage({ id, ts: Date.now() });
  }
}

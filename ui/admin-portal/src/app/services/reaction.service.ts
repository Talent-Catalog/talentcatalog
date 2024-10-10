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

import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {Reaction} from "../model/reaction";
import {RxStompService} from "./rx-stomp.service";
import {map} from "rxjs/operators";

export interface AddReactionRequest {
    emoji: string;
}

@Injectable({
    providedIn: 'root'
})
export class ReactionService {

  constructor(private rxStompService: RxStompService) { }

  /**
   * Subscribe to reaction updates for a given post
   */
  subscribeToReactions(postId: number): Observable<Reaction[]> {
    const topic = `/topic/reaction/${postId}`;
    return this.rxStompService.watch(topic).pipe(
      map((message) => JSON.parse(message.body) as Reaction[])
    );
  }

  /**
   * Add a new reaction to a post via WebSocket
   */
  addReaction(postId: number, request: AddReactionRequest): void {
    const destination = `/app/reaction/${postId}/add`;
    this.rxStompService.publish({
      destination: destination,
      body: JSON.stringify(request)
    });
  }

  /**
   * Modify an existing reaction via WebSocket
   */
  modifyReaction(postId: number, reactionId: number): void {
    const destination = `/app/reaction/${postId}/modify/${reactionId}`;
    this.rxStompService.publish({
      destination: destination
    });
  }

}

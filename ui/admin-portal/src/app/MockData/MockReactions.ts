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

import {Reaction} from "../model/reaction";
import {DisplayUser} from "../model/user";

export class MockReactions implements Reaction {
  id: number;
  emoji: string;
  users: DisplayUser[];

  constructor(id: number, emoji: string, users: DisplayUser[]) {
    this.id = id;
    this.emoji = emoji;
    this.users = users;
  }
}

// Define mock data
export const MOCK_REACTIONS: MockReactions[] = [
  new MockReactions(1, '😊', [{ id: 1, displayName: 'User1' }, { id: 2, displayName: 'User2' }]),
  new MockReactions(2, '👍', [{ id: 3, displayName: 'User3' }]),
  new MockReactions(3, '❤️', [{ id: 4, displayName: 'User4' }, { id: 5, displayName: 'User5' }, { id: 6, displayName: 'User6' }])
];

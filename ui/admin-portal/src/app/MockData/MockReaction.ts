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

export class MockReaction implements Reaction {
  id: number;
  emoji: string;
  users: DisplayUser[];

  constructor(id: number = 1, emoji: string = "👍", users: DisplayUser[] = [new MockUser()]) {
    this.id = id;
    this.emoji = emoji;
    this.users = users;
  }
}

class MockUser implements DisplayUser {
  id: number;
  displayName: string;

  constructor(id: number = 1, displayName: string = "John Doe") {
    this.id = id;
    this.displayName = displayName;
  }
}

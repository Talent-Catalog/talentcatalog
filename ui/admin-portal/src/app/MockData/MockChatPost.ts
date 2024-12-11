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

import {ChatPost} from "../model/chat";
import {MockUser} from "./MockUser";
import {Reaction} from "../model/reaction";
import {MockReaction} from "./MockReaction";

export class MockChatPost implements ChatPost {
  content: string = "Sample content";
  createdBy = new MockUser();
  createdDate: Date = new Date("2024-05-01");
  id: number = 1;
  jobChat: any = {}; // Add mock data for JobChat if available
  updatedBy= new MockUser();
  updatedDate: Date = new Date("2024-05-01");
  reactions?: Reaction[] = [new MockReaction()];
}

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

import {SavedList} from "./saved-list";
import {User} from "./user";
import {MockSavedList} from "../MockData/MockSavedList";
import {MockUser} from "../MockData/MockUser";
import {SavedListLink} from "./saved-list-link";

describe('SavedListLink interface', () => {
  let savedList: SavedList;
  let createdBy: User;
  let updatedBy: User;

  beforeEach(() => {
    savedList = MockSavedList;
    createdBy = new MockUser();
    updatedBy = new MockUser();
  });

  it('should create a valid SavedListLink object', () => {
    const savedListLink: SavedListLink = {
      id: 1,
      savedList: savedList,
      link: 'https://example.com',
      createdBy: createdBy,
      createdDate: Date.now(),
      updatedBy: updatedBy,
      updatedDate: Date.now()
    };

    expect(savedListLink.id).toBe(1);
    expect(savedListLink.savedList).toBe(savedList);
    expect(savedListLink.link).toBe('https://example.com');
    expect(savedListLink.createdBy).toBe(createdBy);
    expect(savedListLink.createdDate).toBeDefined();
    expect(savedListLink.updatedBy).toBe(updatedBy);
    expect(savedListLink.updatedDate).toBeDefined();
  });

});

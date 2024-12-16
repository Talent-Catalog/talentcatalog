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

import {User} from "./user";
import {SavedSearch} from "./saved-search";
import {CandidateReviewStatusItem} from "./candidate-review-status-item";
import {MockUser} from "../MockData/MockUser";
import {MockSavedSearch} from "../MockData/MockSavedSearch";

describe('CandidateReviewStatusItem Model', () => {
  let user: User;
  let savedSearch: SavedSearch;
  let candidateReviewStatusItem: CandidateReviewStatusItem;

  beforeEach(() => {
    user = new MockUser(); // Mock User object
    savedSearch = new MockSavedSearch(); // Mock SavedSearch object

    candidateReviewStatusItem = {
      id: 1,
      savedSearch: savedSearch,
      reviewStatus: 'Pending',
      comment: 'Needs further review',
      createdBy: user,
      createdDate: Date.now(),
      updatedBy: user,
      updatedDate: Date.now(),
    };
  });

  it('should create an instance of CandidateReviewStatusItem', () => {
    expect(candidateReviewStatusItem).toBeTruthy();
  });

  it('should have correct properties', () => {
    expect(candidateReviewStatusItem.id).toBeDefined();
    expect(candidateReviewStatusItem.savedSearch).toBeDefined();
    expect(candidateReviewStatusItem.reviewStatus).toBeDefined();
    expect(candidateReviewStatusItem.comment).toBeDefined();
    expect(candidateReviewStatusItem.createdBy).toBeDefined();
    expect(candidateReviewStatusItem.createdDate).toBeDefined();
    expect(candidateReviewStatusItem.updatedBy).toBeDefined();
    expect(candidateReviewStatusItem.updatedDate).toBeDefined();
  });

  it('should assign correct values to properties', () => {
    expect(candidateReviewStatusItem.id).toBe(1);
    expect(candidateReviewStatusItem.savedSearch).toBe(savedSearch);
    expect(candidateReviewStatusItem.reviewStatus).toBe('Pending');
    expect(candidateReviewStatusItem.comment).toBe('Needs further review');
    expect(candidateReviewStatusItem.createdBy).toBe(user);
    expect(candidateReviewStatusItem.createdDate).toBeTruthy();
    expect(candidateReviewStatusItem.updatedBy).toBe(user);
    expect(candidateReviewStatusItem.updatedDate).toBeTruthy();
  });

  it('should allow modification of properties', () => {
    const newUser = new MockUser();
    const newSavedSearch = new MockSavedSearch();
    const newDate = Date.now();

    candidateReviewStatusItem.savedSearch = newSavedSearch;
    candidateReviewStatusItem.reviewStatus = 'Approved';
    candidateReviewStatusItem.comment = 'Looks good';
    candidateReviewStatusItem.createdBy = newUser;
    candidateReviewStatusItem.createdDate = newDate;
    candidateReviewStatusItem.updatedBy = newUser;
    candidateReviewStatusItem.updatedDate = newDate;

    expect(candidateReviewStatusItem.savedSearch).toBe(newSavedSearch);
    expect(candidateReviewStatusItem.reviewStatus).toBe('Approved');
    expect(candidateReviewStatusItem.comment).toBe('Looks good');
    expect(candidateReviewStatusItem.createdBy).toBe(newUser);
    expect(candidateReviewStatusItem.createdDate).toBe(newDate);
    expect(candidateReviewStatusItem.updatedBy).toBe(newUser);
    expect(candidateReviewStatusItem.updatedDate).toBe(newDate);
  });
});

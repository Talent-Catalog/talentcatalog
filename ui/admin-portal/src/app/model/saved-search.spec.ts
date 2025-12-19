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
import {
  getCandidateSourceNavigation,
  getCandidateSourceStatsNavigation,
  getCandidateSourceType,
  getSavedSearchBreadcrumb,
  getSavedSourceNavigation,
  isSavedSearch,
  SavedSearch,
  SavedSearchSubtype,
  SavedSearchType
} from "./saved-search";
import {CandidateSource, HasId, indexOfHasId} from "./base";

describe('SavedSearch Utilities', () => {
  let location: Location;

  beforeEach(() => {
    // Mock implementations for Location
    location = jasmine.createSpyObj('Location', ['path']);
  });

  describe('Navigation Functions', () => {
    let savedSearch: SavedSearch;
    let candidateSource: CandidateSource;

    beforeEach(() => {
      savedSearch = {
        id: 1,
        name: 'Test Search',
        defaultSearch: false,
        reviewable: true,
        savedSearchType: SavedSearchType.profession,
        savedSearchSubtype: SavedSearchSubtype.engineering,
      } as SavedSearch;

      candidateSource = {
        id: 2,
        name: 'Candidate List',
      } as CandidateSource;
    });

    it('should return correct navigation for getCandidateSourceNavigation', () => {
      const navigation = getCandidateSourceNavigation(savedSearch);
      expect(navigation).toEqual(['search', savedSearch.id]);
    });

    it('should return correct navigation for getCandidateSourceStatsNavigation', () => {
      const navigation = getCandidateSourceStatsNavigation(savedSearch);
      expect(navigation).toEqual(['infographics', 'search', savedSearch.id]);
    });

    it('should return correct navigation for getSavedSourceNavigation', () => {
      const navigation = getSavedSourceNavigation(savedSearch);
      expect(navigation).toEqual(['search', savedSearch.id]);
    });

  });

  describe('Type Checking and Breadcrumbs', () => {
    let savedSearch: SavedSearch;
    let candidateSource: CandidateSource;

    beforeEach(() => {
      savedSearch = {
        id: 1,
        name: 'Test Search (1)',
        defaultSearch: false,
        reviewable: true,
        savedSearchType: SavedSearchType.profession,
        savedSearchSubtype: SavedSearchSubtype.engineering,
        // Assume other properties
      } as SavedSearch;

      candidateSource = {
        id: 2,
        name: 'Candidate List',
        // Assume other properties
      } as CandidateSource;
    });

    it('should correctly identify a SavedSearch using isSavedSearch', () => {
      const result = isSavedSearch(savedSearch);
      expect(result).toBeTrue();
    });

    it('should return correct source type for getCandidateSourceType', () => {
      const type = getCandidateSourceType(savedSearch);
      expect(type).toBe('Search');
    });

    it('should generate correct breadcrumb for getSavedSearchBreadcrumb', () => {
      const infos = [{ savedSearchSubtype: SavedSearchSubtype.engineering, title: 'Engineering' }];
      const breadcrumb = getSavedSearchBreadcrumb(savedSearch, infos);
      expect(breadcrumb).toBe('Search: Test Search (1)');
    });
  });

  describe('Utility Functions', () => {
    let hasIds: HasId[];

    beforeEach(() => {
      hasIds = [{ id: 1 }, { id: 2 }, { id: 3 }];
    });

    it('should return correct index in indexOfHasId', () => {
      const index = indexOfHasId(2, hasIds);
      expect(index).toBe(1);
    });

    it('should return -1 for non-existing id in indexOfHasId', () => {
      const index = indexOfHasId(4, hasIds);
      expect(index).toBe(-1);
    });

  });
});

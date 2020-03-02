import { TestBed } from '@angular/core/testing';

import { SavedSearchResultsCacheService } from './saved-search-results-cache.service';

describe('SavedSearchResultsCacheService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: SavedSearchResultsCacheService = TestBed.get(SavedSearchResultsCacheService);
    expect(service).toBeTruthy();
  });
});

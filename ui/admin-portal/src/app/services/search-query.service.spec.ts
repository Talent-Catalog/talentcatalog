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

import {TestBed} from '@angular/core/testing';
import {SearchQueryService} from './search-query.service';
import {take} from 'rxjs/operators';

describe('SearchQueryService', () => {
  let service: SearchQueryService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SearchQueryService],
    });
    service = TestBed.inject(SearchQueryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return an empty array for an empty query', () => {
    service.changeSearchQuery('');
    service.currentSearchTerms$.pipe(take(1)).subscribe((terms) => {
      expect(terms).toEqual([]);
    });
  });

  it('should return an empty array for a null query', () => {
    service.changeSearchQuery(null);
    service.currentSearchTerms$.pipe(take(1)).subscribe((terms) => {
      expect(terms).toEqual([]);
    });
  });

  it('should parse a simple query into an array of terms', () => {
    const query = 'accountant powerpoint excel';
    service.changeSearchQuery(query);
    service.currentSearchTerms$.pipe(take(1)).subscribe((terms) => {
      expect(terms).toEqual(jasmine.arrayContaining(['accountant', 'excel', 'powerpoint']));
    });
  });


  it('should parse a complex query with phrases and operators', () => {
    const query = 'accountant + (excel powerpoint) "hospital director"';
    service.changeSearchQuery(query);
    service.currentSearchTerms$.pipe(take(1)).subscribe((terms) => {
      expect(terms).toEqual(['hospital director', 'accountant', 'powerpoint', 'excel']);
    });
  });

  it('should remove wildcard characters', () => {
    const query = 'account* + (excel* powerpoint*) "hospital* director*"';
    service.changeSearchQuery(query);
    service.currentSearchTerms$.pipe(take(1)).subscribe((terms) => {
      expect(terms).toEqual(jasmine.arrayContaining(['hospital director', 'account', 'powerpoint', 'excel']));
    });
  });

  it('should handle queries with only operators', () => {
    const query = '+ +';
    service.changeSearchQuery(query);
    service.currentSearchTerms$.pipe(take(1)).subscribe((terms) => {
      expect(terms).toEqual([]);
    });
  });

});

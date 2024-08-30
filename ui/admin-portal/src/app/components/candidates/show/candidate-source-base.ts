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

import {
  CandidateColumnSelectorComponent
} from "../../util/candidate-column-selector/candidate-column-selector.component";

import {ModalDismissReasons, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateFieldService} from "../../../services/candidate-field.service";
import {Input} from "@angular/core";
import {CandidateSource, defaultReviewStatusFilter, DtoType} from "../../../model/base";
import {CandidateFieldInfo} from "../../../model/candidate-field-info";
import {SearchResults} from "../../../model/search-results";
import {Candidate} from "../../../model/candidate";
import {
  CachedSourceResults,
  CandidateSourceResultsCacheService
} from "../../../services/candidate-source-results-cache.service";
import {
  CandidateSourceCandidateService
} from "../../../services/candidate-source-candidate.service";
import {isSavedSearch, SavedSearchGetRequest} from "../../../model/saved-search";
import {SavedListGetRequest} from "../../../model/saved-list";
import {Observable, throwError} from "rxjs";
import {catchError, tap} from "rxjs/operators";

export class CandidateSourceBaseComponent {
  error: any = null;
  longFormat: boolean;

  pageNumber: number;
  pageSize: number;
  results: SearchResults<Candidate>;
  searching: boolean;
  sortField: string;
  sortDirection: string;
  timestamp: number;
  reviewStatusFilter: string[] = defaultReviewStatusFilter;

  @Input() candidateSource: CandidateSource;

  protected selectedFields: CandidateFieldInfo[] = [];

  constructor(
    protected candidateSourceResultsCacheService: CandidateSourceResultsCacheService,
    protected candidateSourceCandidateService: CandidateSourceCandidateService,
    protected candidateFieldService: CandidateFieldService,
    protected modalService: NgbModal
  ) {}

  onSelectColumns() {
    //Initialize with current configuration
    //Output is new configuration
    const modal = this.modalService.open(CandidateColumnSelectorComponent);
    modal.componentInstance.setSourceAndFormat(this.candidateSource, this.longFormat);

    modal.result
    .then(
      () => this.loadSelectedFields()
    )
    .catch(
      error => {
        if (error !== ModalDismissReasons.ESC) {
          this.error = error;
        }
      });
  }

  protected loadSelectedFields() {
    this.selectedFields = this.candidateFieldService
    .getCandidateSourceFields(this.candidateSource, this.longFormat);
  }

  protected checkCache(refresh: boolean, usePageNumber: boolean = true) {
    this.results = null;
    this.error = null;

    //If already searching just exit.
    if (this.searching) {
      return;
    }

    this.searching = true;

    let done = false;
    if (!refresh) {

      //Is there anything in cache?
      //We only cache certain results
      if (this.isCacheable()) {
        const cached: CachedSourceResults =
          this.candidateSourceResultsCacheService.getFromCache(this.candidateSource);
        if (cached) {
          //If we are not required to use the pageNumber (usePageNumber = false)
          //we can take the pageNumber of whatever the cache has.
          //If we have to use the page number, the pageNumber and size must match
          //what is in the cache, or we can't use it.
          done = !usePageNumber
            || (cached.pageNumber === this.pageNumber && cached.pageSize === this.pageSize);
          if (done) {
            this.results = cached.results;
            this.sortField = cached.sortFields[0];
            this.sortDirection = cached.sortDirection;
            this.reviewStatusFilter = []; // We don't cache reviewable sources
            this.timestamp = cached.timestamp;
            this.pageNumber = cached.pageNumber;
            this.pageSize = cached.pageSize;
            this.searching = false;
          }
        }
      }
    }
    return done;
  }

  protected performSearch(
    defaultPageSize: number = 12,
    dtoType: DtoType = DtoType.FULL,
    keyword: string = null,
    showClosedOpps: boolean = false
  ): Observable<SearchResults<Candidate>> {

    this.searching = true;

    // todo Is this the best place to do the defaulting?
    //  Need do defaulting in search request, then pick up actual info from returned results.
    //  Currently server sends back used page number and size but does not echo back sort info.
    //  It should be changed to do so.
    this.pageNumber = this.pageNumber || 1;
    this.pageSize = this.pageSize || defaultPageSize;
    this.sortField = this.sortField || 'id';
    this.sortDirection = this.sortDirection || 'DESC';

    //Create the appropriate request
    let request;
    let reviewable = false;
    if (isSavedSearch(this.candidateSource)) {
      reviewable = this.candidateSource.reviewable;
      request = new SavedSearchGetRequest();
    } else {
      request = new SavedListGetRequest();
    }
    request.keyword = keyword;
    request.showClosedOpps = showClosedOpps;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;
    request.sortFields = [this.sortField];
    request.sortDirection = this.sortDirection;
    request.dtoType = dtoType;
    if (reviewable) {
      request.reviewStatusFilter = this.reviewStatusFilter;
    }

    // Return the observable so the caller can subscribe to it
    return this.candidateSourceCandidateService.searchPaged(this.candidateSource, request).pipe(
      tap(results => {
        this.results = results;
        this.cacheResults();
        this.searching = false;
      }),
      catchError(error => {
        this.error = error;
        this.searching = false;
        return throwError(error);
      })
    );
  }

  private isCacheable(): boolean {
    //Reviewable sources are not cacheable because the review filtering makes it too complicated.
    return !this.isReviewable();
  }

  protected isReviewable(): boolean {
    return isSavedSearch(this.candidateSource)
      ? this.candidateSource.reviewable : false;
  }

  protected cacheResults() {
    this.timestamp = Date.now();

    //We only cache certain results, and we don't cache filter keyword searches
    this.candidateSourceResultsCacheService.cache(this.candidateSource, {
      id: this.candidateSource.id,
      pageNumber: this.pageNumber,
      pageSize: this.pageSize,
      sortFields: [this.sortField],
      sortDirection: this.sortDirection,
      results: this.results,
      timestamp: this.timestamp
    });
  }

}
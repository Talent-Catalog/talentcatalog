/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {getCandidateSourceNavigation, isSavedSearch, SavedSearchGetRequest} from '../../../../model/saved-search';
import {CandidateService} from '../../../../services/candidate.service';
import {Candidate} from '../../../../model/candidate';
import {SearchResults} from '../../../../model/search-results';
import {SavedSearchService} from '../../../../services/saved-search.service';
import {Router} from '@angular/router';
import {
  CachedSourceResults,
  CandidateSourceResultsCacheService
} from '../../../../services/candidate-source-results-cache.service';
import {CandidateSource, defaultReviewStatusFilter, DtoType} from '../../../../model/base';
import {CandidateSourceCandidateService} from '../../../../services/candidate-source-candidate.service';
import {SavedListGetRequest} from '../../../../model/saved-list';
import {AuthorizationService} from '../../../../services/authorization.service';
import {CandidateSourceService} from '../../../../services/candidate-source.service';
import {CandidateFieldInfo} from "../../../../model/candidate-field-info";
import {CandidateFieldService} from "../../../../services/candidate-field.service";
import {
  CandidateColumnSelectorComponent
} from "../../../util/candidate-column-selector/candidate-column-selector.component";
import {ModalDismissReasons, NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-candidate-source-results',
  templateUrl: './candidate-source-results.component.html',
  styleUrls: ['./candidate-source-results.component.scss']
})
export class CandidateSourceResultsComponent implements OnInit, OnChanges {
  error: null;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<Candidate>;
  @Input() candidateSource: CandidateSource;
  @Input() showSourceDetails = true;
  @Output() toggleStarred = new EventEmitter<CandidateSource>();
  @Output() toggleWatch = new EventEmitter<CandidateSource>();
  @Output() copySource = new EventEmitter<CandidateSource>();
  @Output() deleteSource = new EventEmitter<CandidateSource>();
  @Output() editSource = new EventEmitter<CandidateSource>();
  searching: boolean;
  selectedFields: CandidateFieldInfo[] = [];
  sortField: string;
  sortDirection: string;
  timestamp: number;

constructor(
    private authService: AuthorizationService,
    private candidateService: CandidateService,
    private candidateFieldService: CandidateFieldService,
    private candidateSourceService: CandidateSourceService,
    private candidateSourceCandidateService: CandidateSourceCandidateService,
    private modalService: NgbModal,
    private router: Router,
    private savedSearchService: SavedSearchService,
    private candidateSourceResultsCacheService: CandidateSourceResultsCacheService
  ) { };

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    //Do search if candidate source has changed.
    const change = changes.candidateSource;
    if (change && change.previousValue !== change.currentValue) {
      this.loadSelectedFields();
      this.search(false);
    }
  }

  private loadSelectedFields() {
    this.selectedFields = this.candidateFieldService
      .getCandidateSourceFields(this.candidateSource, false);
  }

  onOpenSource() {
    //Open source at same page number
    let extras;
    if (this.pageNumber === 1) {
      extras = {};
    } else {
      extras = {queryParams: {pageNumber: this.pageNumber}};
    }
    const urlCommands = getCandidateSourceNavigation(this.candidateSource);
    this.router.navigate(urlCommands, extras);
  }

  search(refresh: boolean) {
    this.results = null;
    this.timestamp = null;
    this.error = null;

    //If already searching just exit.
    if (this.searching) {
      return;
    }

    this.searching = true;

    let done: boolean = false;
    if (!refresh) {
      const cached: CachedSourceResults =
        this.candidateSourceResultsCacheService.getFromCache(this.candidateSource);
      if (cached) {
        this.results = cached.results;
        this.pageNumber = cached.pageNumber;
        // Keep page size smaller for this component. It is a preview of the source, so doesn't need to be long.
        // It works better on all screen sizes, especially when we have a long split page.
        this.pageSize = 12;
        this.sortField = cached.sortFields[0];
        this.sortDirection = cached.sortDirection;
        this.timestamp = cached.timestamp;
        done = true;
        this.searching = false;
      } else {
        //If there is no cached value, reset all search parameters
        this.pageNumber = 0;
        this.pageSize = 0;
        this.sortField = null;
        this.sortDirection = null;
      }
    }

    if (!done) {
      //todo Is this the best place to do the defaulting?
      //todo Need do defaulting in search request, then pick up actual info
      //from returned results.
      //todo Currently server sends back used page number and size but does
      //not echo back sort info. It should be changed to do so.

      if (!this.pageNumber) {
        this.pageNumber = 1;
      }
      if (!this.pageSize) {
        this.pageSize = 12;
      }
      if (!this.sortField) {
        this.sortField = 'id';
      }
      if (!this.sortDirection) {
        this.sortDirection = 'DESC';
      }

      //Create the appropriate request
      let request;
      let reviewable = false;
      if (isSavedSearch(this.candidateSource)) {
        reviewable = this.candidateSource.reviewable;
        request = new SavedSearchGetRequest();
      } else {
        request = new SavedListGetRequest();
      }
      request.pageNumber = this.pageNumber - 1;
      request.pageSize = this.pageSize;
      request.sortFields = [this.sortField];
      request.sortDirection = this.sortDirection;
      request.dtoType = DtoType.PREVIEW;
      if (reviewable) {
        request.reviewStatusFilter = defaultReviewStatusFilter;
      }

      this.candidateSourceCandidateService.searchPaged(
        this.candidateSource, request).subscribe(
        results => {
          this.timestamp = Date.now();
          this.results = results;

          this.candidateSourceResultsCacheService.cache(this.candidateSource,
            {
            id: this.candidateSource.id,
            pageNumber: this.pageNumber,
            pageSize: this.pageSize,
            sortFields: [this.sortField],
            sortDirection: this.sortDirection,
            results: this.results,
            timestamp: this.timestamp
          });

          this.searching = false;
        },
        error => {
          this.error = error;
          this.searching = false;
        });
    }

  }

  toggleSort(column) {
    if (this.sortField === column) {
      this.sortDirection = this.sortDirection === 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sortField = column;
      this.sortDirection = 'ASC';
    }
    this.search(true);
  }

  //Pass toggle watch up to BrowseCandidateSourcesComponent for it to
  //do the update and refresh its copy of the candidate source details
  // (which is passed through to all contained components)
  onToggleStarred(source: CandidateSource) {
    this.toggleStarred.emit(source);
  }

  //Pass toggle watch up to BrowseCandidateSourcesComponent for it to
  //do the update and refresh its copy of the candidate source details
  // (which is passed through to all contained components)
  onToggleWatch(source: CandidateSource) {
    this.toggleWatch.emit(source);
  }

  onDeleteSource(source: CandidateSource) {
    this.deleteSource.emit(source);
  }

  onEditSource(source: CandidateSource) {
    this.editSource.emit(source);
  }

  onCopySource(source: CandidateSource) {
    this.copySource.emit(source);
  }

  onPageChange() {
    this.search(true);
  }

  onRefreshRequest() {
    this.search(true);
  }

  onSelectColumns() {
    //Initialize with current configuration
    //Output is new configuration
    const modal = this.modalService.open(CandidateColumnSelectorComponent);
    modal.componentInstance.setSourceAndFormat(this.candidateSource, false);

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

  isCandidateNameViewable(): boolean {
    return this.authService.canViewCandidateName();
  }

  isCountryViewable(): boolean {
    return this.authService.canViewCandidateCountry();
  }

  canAccessSalesforce(): boolean {
    return this.authService.canAccessSalesforce();
  }

}

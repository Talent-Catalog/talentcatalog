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
  CandidateColumnSelectorComponent
} from "../../util/candidate-column-selector/candidate-column-selector.component";

import {ModalDismissReasons, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateFieldService} from "../../../services/candidate-field.service";
import {Directive, Input} from "@angular/core";
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
import {isSavedList, isSubmissionList, SavedListGetRequest} from "../../../model/saved-list";
import {Observable, throwError} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {AuthorizationService} from "../../../services/authorization.service";
import {CandidateOpportunity} from "../../../model/candidate-opportunity";

@Directive()
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

  selectedFields: CandidateFieldInfo[] = [];

  constructor(
    protected authorizationService: AuthorizationService,
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
    dtoType: DtoType = DtoType.EXTENDED,
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

    //Use candidateSource to default sort based on whether or not there is a query string
    let defaultSortField = 'id';
    let defaultSortDirection = 'DESC';
    if (isSavedSearch(this.candidateSource)) {
      if (this.candidateSource.simpleQueryString != null) { //todo or empty string
        defaultSortField = 'text' //todo Maybe this should be 'rank' - and give it a column
      }
    }

    this.sortField = this.sortField || defaultSortField;
    this.sortDirection = this.sortDirection || defaultSortDirection;

    //Create the appropriate request
    const request = this.createSearchRequest(keyword, showClosedOpps);
    request.dtoType = dtoType;

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

  createSearchRequest(keyword: string = null, showClosedOpps: boolean = false) {
    let request;
    if (isSavedSearch(this.candidateSource)) {
      const reviewable = this.candidateSource.reviewable;
      request = new SavedSearchGetRequest();
      if (reviewable) {
        request.reviewStatusFilter = this.reviewStatusFilter;
      }
    } else {
      request = new SavedListGetRequest();
      request.keyword = keyword;
      request.showClosedOpps = showClosedOpps;
    }
    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;
    request.sortFields = [this.sortField];
    request.sortDirection = this.sortDirection;

    return request;
  }

  private isCacheable(): boolean {
    //Reviewable sources are not cacheable because the review filtering makes it too complicated.
    return !this.isReviewable();
  }

  isReviewable(): boolean {
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

  protected toggleSort(column: string) {
    if (this.sortField === column) {
      this.sortDirection = this.sortDirection === 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sortField = column;
      this.sortDirection = 'ASC';
    }
  }

  isCvDropDownAvailable(): boolean {
    //Employer partners do not see a Cv drop down option in candidate sources.
    //They can only see CV's (if authorized) in the candidate display
    return !this.authorizationService.isEmployerPartner();
  }

  isCandidateNameViewable(): boolean {
    return this.authorizationService.canViewCandidateName();
  }

  isCountryViewable(): boolean {
    return this.authorizationService.canViewCandidateCountry();
  }

  sourceType(): string {
    return isSavedSearch(this.candidateSource) ? 'savedSearch' : 'list';
  }

  isExportable(): boolean {
    return this.authorizationService.canExportFromSource();
  }

  isImportable(): boolean {
    return isSavedList(this.candidateSource) && this.authorizationService.canImportToList();
  }

  isPublishable(): boolean {
    return isSavedList(this.candidateSource) && this.authorizationService.canPublishList();
  }

  isStarred(): boolean {
    return this.authorizationService.isStarredByMe(this.candidateSource?.users);
  }

  isJobList(): boolean {
    return isSavedList(this.candidateSource) && this.candidateSource.sfJobOpp != null;
  }

  isSubmissionList(): boolean {
    return isSubmissionList(this.candidateSource);
  }

  isShowStage(): boolean {
    return this.isJobList();
  }

  isEditable(): boolean {
    return this.authorizationService.canEditCandidateSource(this.candidateSource);
  }

  isContentModifiable(): boolean {
    let modifiable = false;
    if (!isSavedSearch(this.candidateSource)) {
      if (this.authorizationService.isCandidateSourceMine(this.candidateSource)) {
        modifiable = true
      } else {
        modifiable = !this.authorizationService.isReadOnly();
      }
    }
    return modifiable;
  }

  canAccessSalesforce(): boolean {
    return this.authorizationService.canAccessSalesforce();
  }

  canAccessGoogleDrive(): boolean {
    return this.authorizationService.canAccessGoogleDrive();
  }

  isSalesforceUpdatable(): boolean {
    //Employer partners can't update Salesforce from a candidate source - only from each
    //candidate's display (assuming that they authorized to).
    return !isSavedSearch(this.candidateSource) && !this.authorizationService.isEmployerPartner()
      && this.authorizationService.canUpdateSalesforce() && this.isContentModifiable();
  }

  canAssignTasks() {
    return this.authorizationService.canAssignTask();
  }

  canUpdateStatuses() {
    return this.isContentModifiable() && this.authorizationService.canUpdateCandidateStatus();
  }

  canResolveTasks(): boolean {
    //Employer partners can't resolve outstanding candidate tasks from a list
    return isSavedList(this.candidateSource) && !this.authorizationService.isEmployerPartner()
    && this.authorizationService.canManageCandidateTasks();
  }


  getCandidateOpportunityLink(candidate: Candidate): any[] {
    const opp = this.getCandidateOppForThisJob(candidate);
    return opp ? ['/opp', opp.id] : null;
  }

  /**
   * Get candidate opportunity matching current job
   * @param candidate Candidate who opportunities we need to search
   */
  getCandidateOppForThisJob(candidate: Candidate): CandidateOpportunity {
    return candidate.candidateOpportunities.find(o => o.jobOpp.id === this.candidateSource.sfJobOpp?.id);
  }

  isShareable(): boolean {
    let shareable: boolean = false;

    //Is shareable with me if it is not created by me.
    if (this.candidateSource) {
      //was it created by me?
      if (!this.authorizationService.isCandidateSourceMine(this.candidateSource)) {
        shareable = true;
      }
    }
    return shareable;
  }

  isGlobal(): boolean {
    return this.candidateSource.global;
  }

}

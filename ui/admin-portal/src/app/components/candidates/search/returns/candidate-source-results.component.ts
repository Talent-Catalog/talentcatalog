import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {
  getCandidateSourceNavigation,
  getCandidateSourceType,
  isSavedSearch,
  SavedSearchGetRequest
} from "../../../../model/saved-search";
import {Subscription} from "rxjs";
import {CandidateService} from "../../../../services/candidate.service";
import {Candidate} from "../../../../model/candidate";
import {SearchResults} from "../../../../model/search-results";
import {SavedSearchService} from "../../../../services/saved-search.service";
import {Router} from "@angular/router";
import {
  CachedSearchResults,
  CandidateSourceResultsCacheService
} from "../../../../services/candidate-source-results-cache.service";
import {
  CandidateSource,
  defaultReviewStatusFilter
} from "../../../../model/base";
import {CandidateSourceCandidateService} from "../../../../services/candidate-source-candidate.service";
import {SavedListGetRequest} from "../../../../model/saved-list";

@Component({
  selector: 'app-candidate-source-results',
  templateUrl: './candidate-source-results.component.html',
  styleUrls: ['./candidate-source-results.component.scss']
})
export class CandidateSourceResultsComponent implements OnInit, OnChanges, OnDestroy {
  error: null;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<Candidate>;
  @Input() candidateSource: CandidateSource;
  @Output() toggleWatch = new EventEmitter<CandidateSource>();
  searching: boolean;
  sortField: string;
  sortDirection: string;
  subscription: Subscription;
  timestamp: number;

constructor(
    private candidateService: CandidateService,
    private candidateSourceCandidateService: CandidateSourceCandidateService,
    private router: Router,
    private savedSearchService: SavedSearchService,
    private savedSearchResultsCacheService: CandidateSourceResultsCacheService
  ) { };

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.search(false);
  }

  ngOnDestroy(): void {
    if (this.subscription){
      this.subscription.unsubscribe();
    }
  }

  openSource() {
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
    this.searching = true;

    let done: boolean = false;
    if (!refresh) {
      const cached: CachedSearchResults =
        this.savedSearchResultsCacheService.getFromCache(
          getCandidateSourceType(this.candidateSource),
          this.candidateSource.id, defaultReviewStatusFilter);
      if (cached) {
        this.results = cached.results;
        this.pageNumber = cached.pageNumber;
        this.pageSize = cached.pageSize;
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
        this.pageSize = 20;
      }
      if (!this.sortField) {
        this.sortField = 'id';
      }
      if (!this.sortDirection) {
        this.sortDirection = 'DESC';
      }

      //Create the appropriate request
      let request;
      if (isSavedSearch(this.candidateSource)) {
        request = new SavedSearchGetRequest();
      } else {
        request = new SavedListGetRequest();
      }
      request.pageNumber = this.pageNumber - 1;
      request.pageSize = this.pageSize;
      request.sortFields = [this.sortField];
      request.sortDirection = this.sortDirection;
      if (request instanceof SavedSearchGetRequest) {
        request.reviewStatusFilter = defaultReviewStatusFilter;
      }

      this.candidateSourceCandidateService.searchPaged(
        this.candidateSource, request).subscribe(
        results => {
          this.timestamp = Date.now();
          this.results = results;

          this.savedSearchResultsCacheService.cache(
            getCandidateSourceType(this.candidateSource),
            {
            id: this.candidateSource.id,
            pageNumber: this.pageNumber,
            pageSize: this.pageSize,
            sortFields: [this.sortField],
            sortDirection: this.sortDirection,
            reviewStatusFilter: defaultReviewStatusFilter,
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
  onToggleWatch(source: CandidateSource) {
    this.toggleWatch.emit(source);
  }
}

import {Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges} from '@angular/core';
import {defaultReviewStatusFilter, SavedSearch} from "../../../../model/saved-search";
import {Subscription} from "rxjs";
import {CandidateService} from "../../../../services/candidate.service";
import {Candidate} from "../../../../model/candidate";
import {SearchResults} from "../../../../model/search-results";
import {SavedSearchService} from "../../../../services/saved-search.service";
import {Router} from "@angular/router";
import {
  CachedSearchResults,
  SavedSearchResultsCacheService
} from "../../../../services/saved-search-results-cache.service";

@Component({
  selector: 'app-saved-search-results',
  templateUrl: './saved-search-results.component.html',
  styleUrls: ['./saved-search-results.component.scss']
})
export class SavedSearchResultsComponent implements OnInit, OnChanges, OnDestroy {
  error: null;
  pageNumber: number;
  private pageSize: number;
  results: SearchResults<Candidate>;
  @Input() savedSearch: SavedSearch;
  searching: boolean;
  private sortField: string;
  private sortDirection: string;
  private subscription: Subscription;
  timestamp: number;

constructor(
    private candidateService: CandidateService,
    private router: Router,
    private savedSearchService: SavedSearchService,
    private savedSearchResultsCacheService: SavedSearchResultsCacheService
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

  openSearch() {
    //Open search at same page number, size
    this.router.navigate(['candidates', 'search', this.savedSearch.id],
      {
        queryParams: {pageNumber: this.pageNumber, pageSize: this.pageSize}
      });
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
          this.savedSearch.id, defaultReviewStatusFilter);
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
      this.savedSearchService.load(this.savedSearch.id).subscribe(
        request => {
          this.searchFromRequest(request);
        },
        error => {
          this.error = error;
          this.searching = false;
          // this._loading.savedSearch = false;
        });
    }

  }

  private searchFromRequest(request: any) {

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

    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;
    request.sortFields = [this.sortField];
    request.sortDirection = this.sortDirection;

    //Review status is hard coded for this simple display as we don't want
    //to display or modify review status.
    request.shortlistStatus = defaultReviewStatusFilter;
    this.subscription = this.candidateService.search(request).subscribe(
      results => {
        this.timestamp = Date.now();
        this.results = results;

        this.savedSearchResultsCacheService.cache({
          searchID: this.savedSearch.id,
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

  toggleSort(column) {
    if (this.sortField == column) {
      this.sortDirection = this.sortDirection == 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sortField = column;
      this.sortDirection = 'ASC';
    }
    this.search(true);
  }

}

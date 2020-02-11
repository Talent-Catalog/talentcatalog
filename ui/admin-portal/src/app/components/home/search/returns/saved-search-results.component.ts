import {
  Component,
  Input,
  OnChanges, OnDestroy,
  OnInit,
  SimpleChanges
} from '@angular/core';
import {SavedSearch} from "../../../../model/saved-search";
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
  private error: null;
  private pageNumber: number;
  private pageSize: number;
  private results: SearchResults<Candidate>;
  @Input() savedSearch: SavedSearch;
  private searching: boolean;
  private subscription: Subscription;
  private timestamp: number;

constructor(
    private candidateService: CandidateService,
    private router: Router,
    private savedSearchService: SavedSearchService,
    private savedSearchResultsCacheService: SavedSearchResultsCacheService
  ) { };

  ngOnInit() {
    this.pageNumber = 1;
    this.pageSize = 20;
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


    let done: boolean = false;
    if (!refresh) {
      const cached: CachedSearchResults =
        this.savedSearchResultsCacheService.getFromCache(this.savedSearch.id);
      if (cached) {
        this.results = cached.results;
        this.pageNumber = cached.pageNumber;
        this.timestamp = cached.timestamp;
        //todo What about page size
        done = true;
      }
    }

    if (!done) {
      this.searching = true;
      this.savedSearchService.load(this.savedSearch.id).subscribe(
        request => {
          this.searchFromRequest(request);
        },
        error => {
          this.error = error;
          // this._loading.savedSearch = false;
        });
    }

  }

  private searchFromRequest(request: any) {

    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;
    this.subscription = this.candidateService.search(request).subscribe(
      results => {
        this.timestamp = Date.now();
        this.results = results;

        this.savedSearchResultsCacheService.cache({
          searchID: this.savedSearch.id,
          pageNumber: this.pageNumber,
          pageSize: this.pageSize,
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

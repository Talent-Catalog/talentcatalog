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
import {LocalStorageService} from "angular-2-local-storage";
import {CachedSearchResults} from "../../../../model/cached-search-results";

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
    private localStorage: LocalStorageService,
    private savedSearchService: SavedSearchService
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

  private cacheKey(): string {
    return "Search" + this.savedSearch.id;
  }

  search(refresh: boolean) {
    this.results = null;
    this.timestamp = null;
    this.error = null;


    let done: boolean = false;
    if (!refresh) {
      let cached: CachedSearchResults = JSON.parse(localStorage.getItem(this.cacheKey()));
      if (cached) {
        this.results = cached.results;
        this.pageNumber = cached.pageNumber;
        this.timestamp = cached.timestamp;
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

        let cachedResults: CachedSearchResults = {
          searchID: this.savedSearch.id,
          pageNumber: this.pageNumber,
          pageSize: this.pageSize,
          results: this.results,
          timestamp: this.timestamp
        };
        localStorage.setItem(this.cacheKey(), JSON.stringify(cachedResults));

        this.searching = false;
      },
      error => {
        this.error = error;
        this.searching = false;
      });

  }
}

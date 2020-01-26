import {
  Component,
  Input,
  OnChanges, OnDestroy,
  OnInit,
  SimpleChanges
} from '@angular/core';
import {SavedSearch, SavedSearchRequest} from "../../../../model/saved-search";
import {Subscription} from "rxjs";
import {CandidateService} from "../../../../services/candidate.service";
import {Candidate} from "../../../../model/candidate";
import {SearchResults} from "../../../../model/search-results";
import {Validators} from "@angular/forms";
import {SavedSearchService} from "../../../../services/saved-search.service";
import {SearchCandidateRequest} from "../../../../model/search-candidate-request";

@Component({
  selector: 'app-saved-search-results',
  templateUrl: './saved-search-results.component.html',
  styleUrls: ['./saved-search-results.component.scss']
})
export class SavedSearchResultsComponent implements OnInit, OnChanges, OnDestroy {
  @Input() savedSearch: SavedSearch;
  private searching: boolean;
  public results: SearchResults<Candidate>;
  private error: null;
  private subscription: Subscription;

  constructor(
    private candidateService: CandidateService,
    private savedSearchService: SavedSearchService
  ) { };

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.search();
  }

  ngOnDestroy(): void {
    if (this.subscription){
      this.subscription.unsubscribe();
    }
  }

  search() {
    this.searching = true;
    this.results = null;
    this.error = null;

    this.savedSearchService.load(this.savedSearch.id).subscribe(
      request => {
        this.searchFromRequest(request);
      },
      error => {
        this.error = error;
        // this._loading.savedSearch = false;
      });

  }

  private searchFromRequest(request: SearchCandidateRequest) {

    this.subscription = this.candidateService.search(request).subscribe(
      results => {
        this.results = results;
        this.searching = false;
      },
      error => {
        this.error = error;
        this.searching = false;
      });

  }
}

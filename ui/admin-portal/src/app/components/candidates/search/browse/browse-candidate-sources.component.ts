import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges
} from '@angular/core';
import {SearchResults} from '../../../../model/search-results';
import {FormBuilder, FormGroup} from '@angular/forms';
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';
import {
  indexOfAuditable,
  SavedSearch,
  SavedSearchSubtype,
  SavedSearchType,
  SearchSavedSearchRequest
} from '../../../../model/saved-search';
import {SavedSearchService} from '../../../../services/saved-search.service';
import {Router} from '@angular/router';
import {LocalStorageService} from "angular-2-local-storage";
import {AuthService} from "../../../../services/auth.service";
import {User} from "../../../../model/user";
import {
  CandidateSource,
  CandidateSourceType,
  SearchBy,
  SearchCandidateSourcesRequest
} from "../../../../model/base";
import {SearchSavedListRequest} from "../../../../model/saved-list";
import {CandidateSourceService} from "../../../../services/candidate-source.service";

@Component({
  selector: 'app-browse-candidate-sources',
  templateUrl: './browse-candidate-sources.component.html',
  styleUrls: ['./browse-candidate-sources.component.scss']
})
export class BrowseCandidateSourcesComponent implements OnInit, OnChanges {

  private savedStateKeyPrefix: string = 'BrowseKey';

  @Input() sourceType: CandidateSourceType;
  @Input() searchBy: SearchBy;
  @Input() savedSearchType: SavedSearchType;
  @Input() savedSearchSubtype: SavedSearchSubtype;
  searchForm: FormGroup;
  public loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<CandidateSource>;
  selectedSource: CandidateSource;
  selectedIndex = 0;
  private loggedInUser: User;

  constructor(private fb: FormBuilder,
              private localStorageService: LocalStorageService,
              private router: Router,
              private authService: AuthService,
              private candidateSourceService: CandidateSourceService,
              private savedSearchService: SavedSearchService) {
  }

  ngOnInit() {

    this.loggedInUser = this.authService.getLoggedInUser();

    this.searchForm = this.fb.group({
      keyword: ['']
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    this.onChanges();
  }

  get keyword(): string {
    return this.searchForm ? this.searchForm.value.keyword : "";
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.search();
  }

  onChanges(): void {
    this.searchForm.valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged()
      )
      .subscribe(res => {
        this.search();
      });
    this.search();
  }

  search() {
    let req: SearchCandidateSourcesRequest;
    if (this.sourceType === CandidateSourceType.SavedSearch) {
      req = new SearchSavedSearchRequest();
    } else {
      req = new SearchSavedListRequest();
    }
    req.keyword = this.keyword;
    req.pageNumber = this.pageNumber - 1;
    req.pageSize = this.pageSize;
    req.sortFields = ['name'];
    req.sortDirection = 'ASC';
    req.pageNumber = this.pageNumber - 1;
    req.pageSize = this.pageSize;
    req.sortFields = ['name'];
    req.sortDirection = 'ASC';
    switch (this.searchBy) {
      case SearchBy.mine:
        req.owned = true;
        break;
      case SearchBy.sharedWithMe:
        req.shared = true;
        break;
      case SearchBy.all:
        req.fixed = true;
        req.owned = true;
        req.shared = true;
        break;
    }
    if (this.savedSearchType !== undefined) {
      if (req instanceof SearchSavedSearchRequest) {
        req.savedSearchType = this.savedSearchType;
        req.savedSearchSubtype = this.savedSearchSubtype;
        req.fixed = true;
        req.owned = true;
        req.shared = true;
      }
    }

    this.loading = true;

    this.candidateSourceService.searchPaged(req).subscribe(results => {
      this.results = results;

      if (results.content.length > 0) {
        //Selected previously search if any
        const savedSearchID: number = this.localStorageService.get(this.savedStateKey());
        if (savedSearchID) {
          this.selectedIndex = indexOfAuditable(savedSearchID, this.results.content);
          if (this.selectedIndex >= 0) {
            this.selectedSource = this.results.content[this.selectedIndex];
          } else {
            //Select the first search if can't find previous (category of search
            // may have changed)
            this.onSelect(this.results.content[0]);
          }
        } else {
          //Select the first search if no previous
          this.onSelect(this.results.content[0]);
        }
      }

      this.loading = false;
    },
    error => {
      this.error = error;
      this.loading = false;
    });
  }

  onSelect(savedSearch: CandidateSource) {
    this.selectedSource = savedSearch;

    const savedSearchID: number = savedSearch.id;
    this.localStorageService.set(this.savedStateKey(), savedSearchID);

    this.selectedIndex = indexOfAuditable(savedSearchID, this.results.content);
  }

  private savedStateKey() {
    //todo Different key for non saved searches
    return this.savedStateKeyPrefix + this.savedSearchType +
      (this.savedSearchSubtype ? '/' + this.savedSearchSubtype : "");
  }

  keyDown(event: KeyboardEvent) {
    const oldSelectedIndex = this.selectedIndex;
    switch (event.key) {
      case 'ArrowUp':
        if (this.selectedIndex > 0) {
          this.selectedIndex--;
        }
        break;
      case 'ArrowDown':
        if (this.selectedIndex < this.results.content.length - 1) {
          this.selectedIndex++;
        }
        break;
    }
    if (this.selectedIndex !== oldSelectedIndex) {
      this.onSelect(this.results.content[this.selectedIndex])
    }
  }

  onToggleWatch(savedSearch: SavedSearch) {
    this.loading = true;
    if (this.isWatching(savedSearch)) {
      this.savedSearchService
        .removeWatcher(savedSearch.id, {userId: this.loggedInUser.id})
        .subscribe(result => {
          //Update local copy
          this.updateLocalSavedSearchCopy(result);
          this.loading = false;
        }, err => {
          this.loading = false;
          this.error = err;
        })
    } else {
      this.savedSearchService
        .addWatcher(savedSearch.id, {userId: this.loggedInUser.id})
        .subscribe(result => {
          this.updateLocalSavedSearchCopy(result);
          this.loading = false;
        }, err => {
          this.loading = false;
          this.error = err;
        })
    }
  }

  private isWatching(savedSearch: SavedSearch): boolean {
    return savedSearch.watcherUserIds.indexOf(this.loggedInUser.id) >= 0;
  }

  private updateLocalSavedSearchCopy(savedSearch: SavedSearch) {
    const index: number = indexOfAuditable(savedSearch.id, this.results.content);
    if (index >= 0) {
      this.results.content[index] = savedSearch;
    }
    if (this.selectedIndex === index) {
      this.selectedSource = savedSearch;
    }
  }
}

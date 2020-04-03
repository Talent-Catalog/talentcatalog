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
  indexOfSavedSearch,
  SavedSearch,
  SavedSearchSubtype,
  SavedSearchType,
  SearchBy
} from '../../../../model/saved-search';
import {SavedSearchService} from '../../../../services/saved-search.service';
import {Router} from '@angular/router';
import {LocalStorageService} from "angular-2-local-storage";

@Component({
  selector: 'app-browse-saved-searches',
  templateUrl: './browse-saved-searches.component.html',
  styleUrls: ['./browse-saved-searches.component.scss']
})
export class BrowseSavedSearchesComponent implements OnInit, OnChanges {

  private savedStateKeyPrefix: string = 'BrowseKey';

  @Input() searchBy: SearchBy;
  @Input() savedSearchType: SavedSearchType;
  @Input() savedSearchSubtype: SavedSearchSubtype;
  searchForm: FormGroup;
  public loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<SavedSearch>;
  selectedSavedSearch: SavedSearch;
  selectedIndex = 0;

  constructor(private fb: FormBuilder,
              private localStorageService: LocalStorageService,
              private router: Router,
              private savedSearchService: SavedSearchService) {
  }

  ngOnInit() {

    this.searchForm = this.fb.group({
      keyword: ['']
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    this.onChanges();
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
    if (this.savedSearchType == undefined) {
      this.error = "Haven't implemented search by " + SearchBy[this.searchBy];
    } else {
      this.loading = true;
      const request = this.searchForm ? this.searchForm.value : {keyword: ""};
      request.savedSearchType = this.savedSearchType;
      request.savedSearchSubtype = this.savedSearchSubtype;
      request.pageNumber = this.pageNumber - 1;
      request.pageSize = this.pageSize;
      request.sortFields = ['name'];
      request.sortDirection = 'ASC';
      this.savedSearchService.search(request).subscribe(results => {
        this.results = results;

        if (results.content.length > 0) {
          //Selected previously search if any
          const savedSearchID: number = this.localStorageService.get(this.savedStateKey());
          if (savedSearchID) {
            this.selectedIndex = indexOfSavedSearch(savedSearchID, this.results.content);
            if (this.selectedIndex >= 0) {
              this.selectedSavedSearch = this.results.content[this.selectedIndex];
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
      });
    }
  }

  onSelect(savedSearch: SavedSearch) {
    this.selectedSavedSearch = savedSearch;

    let savedSearchID: number = savedSearch.id;
    this.localStorageService.set(this.savedStateKey(), savedSearchID);

    this.selectedIndex = indexOfSavedSearch(savedSearchID, this.results.content);
  }

  private savedStateKey() {
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
    if (this.selectedIndex != oldSelectedIndex) {
      this.onSelect(this.results.content[this.selectedIndex])
    }
  }
}

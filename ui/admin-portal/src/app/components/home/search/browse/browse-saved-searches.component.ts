import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';


import {SearchResults} from '../../../../model/search-results';

import {FormBuilder, FormGroup} from '@angular/forms';
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';
import {SavedSearch, SavedSearchType} from '../../../../model/saved-search';
import {SavedSearchService} from '../../../../services/saved-search.service';
import {Router} from '@angular/router';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

//todo Use local storage
//todo Display loading
//todo Support paging/sorting request
//todo Fix up types to match Java types.

@Component({
  selector: 'app-browse-saved-searches',
  templateUrl: './browse-saved-searches.component.html',
  styleUrls: ['./browse-saved-searches.component.scss']
})
export class BrowseSavedSearchesComponent implements OnInit {

  @Input() savedSearchType: SavedSearchType;
  searchForm: FormGroup;
  public loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<SavedSearch>;
  selectedSavedSearch: SavedSearch;
  selectedIndex = 0;

  constructor(private fb: FormBuilder,
              private router: Router,
              private savedSearchService: SavedSearchService,
              private modalService: NgbModal) {
  }

  ngOnInit() {

    this.searchForm = this.fb.group({
      keyword: ['']
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    this.onChanges();
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
    this.loading = true;
    const request = this.searchForm.value;
    request.savedSearchType = this.savedSearchType;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;
    this.savedSearchService.search(request).subscribe(results => {
      this.results = results;
      this.loading = false;
    });
  }

  //todo When change is by mouse click, need to compute index.
  onSelect(savedSearch: SavedSearch) {
    this.selectedSavedSearch = savedSearch;
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

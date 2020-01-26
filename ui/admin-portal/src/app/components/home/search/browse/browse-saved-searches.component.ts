import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';


import {SearchResults} from '../../../../model/search-results';

import {FormBuilder, FormGroup} from '@angular/forms';
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';
import {SavedSearch, SavedSearchType} from '../../../../model/saved-search';
import {SavedSearchService} from '../../../../services/saved-search.service';
import {Router} from '@angular/router';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

//todo Add support for arrows selection
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

  onSelect(savedSearch: SavedSearch) {
    this.selectedSavedSearch = savedSearch;
  }
}

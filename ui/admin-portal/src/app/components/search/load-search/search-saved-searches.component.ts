import {Component, OnInit} from '@angular/core';


import {SearchResults} from '../../../model/search-results';

import {FormBuilder, FormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {SavedSearch} from "../../../model/saved-search";
import {SavedSearchService} from "../../../services/saved-search.service";
import {Router} from "@angular/router";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-search-saved-searches',
  templateUrl: './search-saved-searches.component.html',
  styleUrls: ['./search-saved-searches.component.scss']
})
export class SearchSavedSearchesComponent implements OnInit {

  searchForm: FormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<SavedSearch>;
  selectedSearch;


  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private router: Router,
              private savedSearchService: SavedSearchService) {
  }

  ngOnInit() {

    this.searchForm = this.fb.group({
      keyword: ['']
    });
    this.pageNumber = 1;
    this.pageSize = 6;

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

  search(){
    this.loading = true;
    let request = this.searchForm.value;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;
    request.fixed = false;
    request.owned = true;
    request.shared = true;
    this.savedSearchService.search(request).subscribe(results => {
      this.results = results;
      this.loading = false;
    });
  }

  selectSearch(savedSearch){
     this.selectedSearch = savedSearch;
     this.closeModal();
  }

  closeModal() {
    this.activeModal.close(this.selectedSearch);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}

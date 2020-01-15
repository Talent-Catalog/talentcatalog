import {Component, Input, OnInit} from '@angular/core';


import {SearchResults} from '../../../model/search-results';

import {FormBuilder, FormGroup} from '@angular/forms';
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';
import {SavedSearch, SavedSearchType} from '../../../model/saved-search';
import {SavedSearchService} from '../../../services/saved-search.service';
import {Router} from '@angular/router';
import {EditSavedSearchComponent} from './edit/edit-saved-search.component';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";

@Component({
  selector: 'app-saved-searches',
  templateUrl: './saved-searches.component.html',
  styleUrls: ['./saved-searches.component.scss']
})
export class SavedSearchesComponent implements OnInit {

  @Input() savedSearchType: SavedSearchType;
  searchForm: FormGroup;
  public loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<SavedSearch>;
  // selectedSearch;


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

  search(){
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

  openSearch(savedSearch){
    this.router.navigate(['candidates', 'search', savedSearch.id]);
  }

  editSavedSearch(savedSearch) {
    const editSavedSearchModal = this.modalService.open(EditSavedSearchComponent, {
      centered: true,
      backdrop: 'static'
    });

    editSavedSearchModal.componentInstance.savedSearchId = savedSearch.id;

    editSavedSearchModal.result
      .then((savedSearch) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  deleteSavedSearch(savedSearch) {
    const deleteSavedSearchModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteSavedSearchModal.componentInstance.message = 'Are you sure you want to delete '+savedSearch.name;

    deleteSavedSearchModal.result
      .then((result) => {
        console.log(result);
        if (result === true) {
          this.savedSearchService.delete(savedSearch.id).subscribe(
            (savedSearch) => {
              this.loading = false;
              this.search();
            },
            (error) => {
              this.error = error;
              this.loading = false;
            });
          this.search()
        }
      })
      .catch(() => { /* Isn't possible */ });
  }

}

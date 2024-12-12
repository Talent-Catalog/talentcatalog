/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {SearchResults} from '../../../model/search-results';
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from "rxjs/operators";
import {SavedSearch} from '../../../model/saved-search';
import {SavedSearchService} from "../../../services/saved-search.service";
import {Router} from "@angular/router";
import {Observable, of} from "rxjs";
import {SearchCandidateSourcesRequest} from "../../../model/base";

@Component({
  selector: 'app-join-saved-search',
  templateUrl: './join-saved-search.component.html',
  styleUrls: ['./join-saved-search.component.scss']
})
export class JoinSavedSearchComponent implements OnInit, OnChanges {

  searchForm: UntypedFormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<SavedSearch>;
  searching = false;
  searchFailed = false;
  doSavedSearchSearch;
  currentSavedSearchId: number;
  selectedBaseSearch: SavedSearch;
  selectedSearchId: number;
  @Input() baseSearch;
  @Input() readonly: boolean;
  @Output() addBaseSearch = new EventEmitter<SavedSearch>();
  @Output() deleteBaseSearch = new EventEmitter();

  constructor(private fb: UntypedFormBuilder,
              private router: Router,
              private savedSearchService: SavedSearchService) {
  }

  ngOnInit() {

    this.searchForm = this.fb.group({
      selectedSavedSearch: [null],
    });

    //dropdown to add joined searches
    this.doSavedSearchSearch = (text$: Observable<string>) =>
      text$.pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => {
          this.searching = true;
          this.error = null
        }),
        switchMap(term => {
            const request: SearchCandidateSourcesRequest = {
              keyword: term, global: true, owned: true, shared: true
            }
            return this.savedSearchService.searchPaged(request).pipe(
              tap(() => this.searchFailed = false),
              map(result =>
                // filter to avoid circular reference exception by removing the same search as the loaded search
                result.content.filter(content =>
                  content.id !== this.currentSavedSearchId)),
              catchError(() => {
                this.searchFailed = true;
                return of([]);
              }));
          }
        ),
        tap(() => this.searching = false)
      );

    this.renderSavedSearchRow(this.selectedBaseSearch)

  }

  ngOnChanges (changes: SimpleChanges) {
    // If clear search is selected, remove the base search
    if (changes && changes.baseSearch && changes.baseSearch.currentValue === null){
      this.selectedBaseSearch = null;
    }

    // If base search loaded from database (memory)
    if (changes && changes.baseSearch && changes.baseSearch.previousValue === null
      && changes.baseSearch.currentValue !== null && this.selectedBaseSearch === null) {
      this.selectedBaseSearch = changes.baseSearch.currentValue;
    }
  }

  renderSavedSearchRow(savedSearch: SavedSearch) {
    return savedSearch?.name;
  }


  selected(selectedSearchId: number) {
    this.savedSearchService.get(selectedSearchId).subscribe(result => {
      this.selectedBaseSearch = result;
      this.addBaseSearch.emit(this.selectedBaseSearch);
    })
  }

  deleteSearch(){
    this.selectedBaseSearch = null;
    this.deleteBaseSearch.emit();
  }

}

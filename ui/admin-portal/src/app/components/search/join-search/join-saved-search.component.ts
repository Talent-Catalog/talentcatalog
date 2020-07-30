import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {SearchResults} from '../../../model/search-results';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  map,
  switchMap,
  tap
} from "rxjs/operators";
import {SavedSearch, SavedSearchJoin} from '../../../model/saved-search';
import {SavedSearchService} from "../../../services/saved-search.service";
import {Router} from "@angular/router";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {Observable, of} from "rxjs";
import {CandidateOccupation} from "../../../model/candidate-occupation";

@Component({
  selector: 'app-join-saved-search',
  templateUrl: './join-saved-search.component.html',
  styleUrls: ['./join-saved-search.component.scss']
})
export class JoinSavedSearchComponent implements OnInit, OnChanges {

  searchForm: FormGroup;
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
  @Output() addBaseSearch = new EventEmitter<SavedSearch>();
  @Output() deleteBaseSearch = new EventEmitter();

  constructor(private fb: FormBuilder,
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
        switchMap(term =>
          this.savedSearchService.searchPaged({keyword: term, global: true, owned: true, shared: true}).pipe(
            tap(() => this.searchFailed = false),
            map(result =>
              // filter to avoid circular reference exception by removing the same search as the loaded search
              result.content.filter(content =>
                content.id !== this.currentSavedSearchId)),
            catchError(() => {
              this.searchFailed = true;
              return of([]);
            }))
        ),
        tap(() => this.searching = false)
      );

  }

  ngOnChanges (changes: SimpleChanges) {
    // If clear search is selected, remove the base search
    if (changes && changes.baseSearch && changes.baseSearch.currentValue === null){
      this.selectedBaseSearch = null;
    }

    // If base search loaded from database (memory)
    if (changes && changes.baseSearch && changes.baseSearch.previousValue === null && changes.baseSearch.currentValue !== null && this.selectedBaseSearch === null) {
      this.selected(changes.baseSearch.currentValue.id);
    }
  }

  renderSavedSearchRow(savedSearch: SavedSearch) {
    return '';
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

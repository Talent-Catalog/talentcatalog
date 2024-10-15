import {Component, EventEmitter, Input, OnInit, Output,} from '@angular/core';
import {Observable, of, Subject} from "rxjs";
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from "rxjs/operators";
import {CandidateSourceService} from "../../../services/candidate-source.service";
import {
  CandidateSource,
  CandidateSourceType,
  SearchCandidateSourcesRequest
} from "../../../model/base";

/**
 * Finds a source by name by searching the server based on what is typed into an input field.
 * <p/>
 * Based on https://ng-select.github.io/ng-select#/search
 */

@Component({
  selector: 'app-find-candidate-source',
  templateUrl: './find-candidate-source.component.html',
  styleUrls: ['./find-candidate-source.component.scss']
})
export class FindCandidateSourceComponent implements OnInit {
  //Whether single selection only - default is multiple selection allowed
  @Input() single: boolean = false;
  @Input() sourceType: CandidateSourceType;
  @Input() id: number;
  @Input() ids: number[];
  @Input() fixed: boolean;
  @Input() global: boolean;
  @Input() owned: boolean;
  @Input() shared: boolean;

  @Output() selectionMade =  new EventEmitter<CandidateSource>();
  @Output() selectionsMade =  new EventEmitter<CandidateSource[]>();

  sources$: Observable<CandidateSource[]>;
  sourceNameInput$ = new Subject<string>();

  //Used for single selection
  currentSelection: CandidateSource;

  //Used for multiple selection
  currentSelections: CandidateSource[] = [];

  searching: boolean;

  constructor(private candidateSourceService: CandidateSourceService) {
  }

  ngOnInit(): void {
    this.loadExistingSelection();
  }

  private loadExistingSelection() {
    //If there is already a source associated set the selection to it, otherwise clear selection.
    if (this.single && this.id) {
      this.candidateSourceService.get(this.sourceType, this.id).subscribe({
        next: source => {
          this.setCurrentSelection([source]);
          this.loadSources();
        }
      });
    } else if (!this.single && this.ids) {
      //todo This needs to allow multiple initial values  - need getShortInfos IdsRequest
    } else {
      this.clearSelection()
      this.loadSources();
    }
  }

  private clearSelection() {
    this.setCurrentSelection([]);
  }

  private setCurrentSelection(sources: CandidateSource[]) {
    this.currentSelections = sources;
    this.currentSelection = sources  && sources.length > 0 ? sources[0] : null;
  }

  /**
   * This fetches sources based on their names as the user types.
   * @private
   */
  private loadSources() {
    this.sources$ = this.sourceNameInput$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      tap(() => this.searching = true),
      switchMap((term) => this.doSearch(term)),
      tap(() => this.searching = false)
    );
  }

  private doSearch(text: string): Observable<CandidateSource[]> {
    if (this.sourceType == null) {
      return of([]);
    } else {
      let request: SearchCandidateSourcesRequest = {
        keyword: text,
        pageSize: 10,
        fixed: this.fixed,
        global: this.global,
        owned: this.owned,
        shared: this.shared
      }
      return this.candidateSourceService.searchPaged(this.sourceType, request).pipe(
        map(results => results.content),
        catchError(() => {
          return of([]);
        }))
    }
  }

  onChangedSelection($event: any) {
    if (this.single) {
      this.selectionMade.emit($event);
    } else {
      this.selectionsMade.emit($event);
    }
  }
}

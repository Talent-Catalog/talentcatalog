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
  SimpleChanges,
} from '@angular/core';
import {Observable, of, Subject} from "rxjs";
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from "rxjs/operators";
import {CandidateSourceService} from "../../../services/candidate-source.service";
import {
  CandidateSource,
  CandidateSourceType,
  DtoType,
  IdsRequest,
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
export class FindCandidateSourceComponent implements OnInit, OnChanges {
  //Whether single selection only - default is multiple selection allowed
  @Input() single: boolean = false;
  @Input() sourceType: CandidateSourceType;
  @Input() selectedIds: number[] | number;
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
    this.loadSources();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.changeSelection();
  }

  private changeSelection() {
    //If there is already a source associated set the selection to it, otherwise clear selection.
    if (this.selectedIds) {
      let idsRequest: IdsRequest = Array.isArray(this.selectedIds) ? {ids: this.selectedIds} : {ids: [this.selectedIds]};
      idsRequest.dtoType = DtoType.MINIMAL;
      this.candidateSourceService.searchByIds(this.sourceType, idsRequest).subscribe({
        next: (sources: CandidateSource[])  => {
          this.setCurrentSelection(sources);
        }
      });
    } else {
      this.clearSelection()
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
        dtoType: DtoType.MINIMAL,
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

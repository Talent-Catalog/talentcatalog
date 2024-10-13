import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {Observable, of} from "rxjs";
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from "rxjs/operators";
import {NgbTypeaheadSelectItemEvent} from "@ng-bootstrap/ng-bootstrap";
import {CandidateSourceService} from "../../../services/candidate-source.service";
import {
  CandidateSource,
  CandidateSourceType,
  SearchCandidateSourcesRequest
} from "../../../model/base";

/**
 * Finds a source by name by searching the server based on what is typed into an input field.
 * <p/>
 * Based on https://ng-bootstrap.github.io/#/components/typeahead/examples
 */

//todo Needs to be renamed to FindSourceComponent
@Component({
  selector: 'app-find-list',
  templateUrl: './find-list.component.html',
  styleUrls: ['./find-list.component.scss']
})
export class FindListComponent implements OnInit, OnChanges {

  @Input() id: number;
  @Input() fixed: boolean;
  @Input() global: boolean;
  @Input() owned: boolean;
  @Input() shared: boolean;
  @Output() selectionMade =  new EventEmitter<CandidateSource>();

  currentSelection: CandidateSource;

  //This is set in ngOnInit to the function called from the html input ngbTypeahead.
  //(Note that calling a method does not work because "this" is undefined - instead of referring
  //to this component instance - meaning that you can't access properties of this component - JC)
  doFind;

  searching: boolean;

  //todo Currently hard coded for lists but should also work for searches
  private sourceType:CandidateSourceType = CandidateSourceType.SavedList;

  constructor(private candidateSourceService: CandidateSourceService) {
  }

  ngOnInit(): void {
    //See https://ng-bootstrap.github.io/#/components/typeahead/examples
    this.doFind = (text$: Observable<string>) =>
      text$.pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => {
          this.searching = true;
        }),
        switchMap(text => {
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
        ),
        tap(() => this.searching = false)
      );
  }

  ngOnChanges(changes: SimpleChanges): void {

    //If there is already a source associated, get its name and construct the default
    //job request (ie to retain existing job)
    if (this.id) {
      this.candidateSourceService.get(this.sourceType, this.id).subscribe({
        next: source => this.setCurrentSelection(source)
      });
    } else {
      this.clearSelection()
    }
  }

  renderSource(source: CandidateSource) {
    return source.name;
  }

  clearSelection() {
    this.setCurrentSelection(null);
  }

  selectResult($event: NgbTypeaheadSelectItemEvent<any>) {
    this.setCurrentSelection($event.item);
  }

  private setCurrentSelection(source: CandidateSource) {
    this.currentSelection = source;
    this.emitCurrentSelection();
  }

  private emitCurrentSelection() {
    this.selectionMade.emit(this.currentSelection);
  }
}

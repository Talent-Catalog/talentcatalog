import {Component, OnInit} from '@angular/core';
import {Observable, of} from "rxjs";
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from "rxjs/operators";
import {NgbTypeaheadSelectItemEvent} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-find-list',
  templateUrl: './find-list.component.html',
  styleUrls: ['./find-list.component.scss']
})
export class FindListComponent implements OnInit {

  //This is set in ngOnInit to the function called from the html input ngbTypeahead.
  //(Note that calling a method does not work because "this" is undefined - instead of referring
  //to this component instance - meaning that you can't access properties of this component - JC)
  doFind;

  constructor() { }

  ngOnInit(): void {
    //See https://ng-bootstrap.github.io/#/components/typeahead/examples
    this.doFind = (text$: Observable<string>) =>
      text$.pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => {
          this.searching = true;
        }),
        switchMap(text =>
          this.jobService.searchPaged(
            {keyword: text, jobNameAndIdOnly: true, pageSize: 10}).pipe(
            map(result => result.content),
            catchError(() => {
              return of([]);
            }))
        ),
        tap(() => this.searching = false)
      );
  }

  selectResult($event: NgbTypeaheadSelectItemEvent<any>) {
    this.currentJobRequest = $event.item;
    this.emitCurrentJobRequest();
  }

  private emitCurrentJobRequest() {
    this.jobSelection.emit(this.currentJobRequest);
  }

}

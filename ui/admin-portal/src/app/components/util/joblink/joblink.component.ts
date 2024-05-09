/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import {Observable, of} from "rxjs";
import {JobNameAndId} from "../../../model/job";
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from "rxjs/operators";
import {JobService} from "../../../services/job.service";
import {NgbTypeaheadSelectItemEvent} from "@ng-bootstrap/ng-bootstrap";

//See https://ng-bootstrap.github.io/#/components/typeahead/examples
@Component({
  selector: 'app-joblink',
  templateUrl: './joblink.component.html',
  styleUrls: ['./joblink.component.scss']
})
export class JoblinkComponent implements OnInit, OnChanges {

  @Input() jobId: number;
  @Output() jobSelection =  new EventEmitter<JobNameAndId>();

  //Name matching current jobId
  jobName: string;

  //This is set in ngOnInit to the function called from the html input ngbTypeahead.
  //(Note that calling a method does not work because "this" is undefined - instead of referring
  //to this component instance - meaning that you can't access properties of this component - JC)
  doJobSearch;

  searching: boolean;

  constructor(private jobService: JobService) {
  }

  ngOnInit(): void {
    this.doJobSearch = (text$: Observable<string>) =>
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

  ngOnChanges(changes: SimpleChanges): void {
    if (this.jobId) {
      this.jobService.get(this.jobId).subscribe({
        next: job => this.jobName = job.name
      });
    }
  }

  renderJobRow(job: JobNameAndId) {
    return job.name;
  }

  removeJob() {
    this.jobSelection.emit(null);
  }

  selectSearchResult($event: NgbTypeaheadSelectItemEvent<any>) {
      const job: JobNameAndId = $event.item;
      this.jobSelection.emit(job);
  }
}

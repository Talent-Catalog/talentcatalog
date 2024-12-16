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
import {Observable, of} from "rxjs";
import {JobNameAndId} from "../../../model/job";
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from "rxjs/operators";
import {JobService} from "../../../services/job.service";
import {NgbTypeaheadSelectItemEvent} from "@ng-bootstrap/ng-bootstrap";

// todo Change this to app-find-job to match app-find-candidate-source.

@Component({
  selector: 'app-joblink',
  templateUrl: './joblink.component.html',
  styleUrls: ['./joblink.component.scss']
})
export class JoblinkComponent implements OnInit, OnChanges {

  // todo This is not used anywhere now
  @Input() jobId: number;
  @Output() jobSelection =  new EventEmitter<JobNameAndId>();

  //This is set in ngOnInit to the function called from the html input ngbTypeahead.
  //(Note that calling a method does not work because "this" is undefined - instead of referring
  //to this component instance - meaning that you can't access properties of this component - JC)
  doJobSearch;

  //Job name associated with jobId
  currentJobName: string;

  //Current update request
  currentJobRequest: JobNameAndId;

  //True if removeJob is currently checked.
  removeJobRequest: boolean;

  //Heading displayed for job search - depends on whether there is already a job associated
  searchHeading: string;

  //True if searching for jobs
  searching: boolean;

  constructor(private jobService: JobService) {
  }

  ngOnInit(): void {

    //See https://ng-bootstrap.github.io/#/components/typeahead/examples
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
    //Display appropriate heading for job search
    this.searchHeading = (this.jobId ? "Change" : "Optional") + " job association";

    //If there is already a job associated, get its name and construct the default
    //job request (ie to retain existing job)
    if (this.jobId) {
      this.jobService.get(this.jobId).subscribe({
        next: job => {
          this.currentJobRequest = {
            name: job.name,
            id: job.id
          }
          this.currentJobName = job.name;
        }
      });
    }
  }

  renderJobRow(job: JobNameAndId) {
    return job.name;
  }

  removeJob($event) {
    this.removeJobRequest = $event.target.checked;
    if (this.removeJobRequest) {
      this.currentJobRequest = null;
    } else {
      this.currentJobRequest = {
        name: this.currentJobName,
        id: this.jobId
      }
    }
    this.emitCurrentJobRequest();
  }

  selectSearchResult($event: NgbTypeaheadSelectItemEvent<any>) {
      this.currentJobRequest = $event.item;
      this.emitCurrentJobRequest();
  }

  private emitCurrentJobRequest() {
    this.jobSelection.emit(this.currentJobRequest);
  }
}

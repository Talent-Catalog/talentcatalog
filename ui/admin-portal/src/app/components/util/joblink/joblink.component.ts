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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
} from '@angular/forms';
import {FormComponentBase} from "../form/FormComponentBase";
import {Observable, of} from "rxjs";
import {JobNameAndId} from "../../../model/job";
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from "rxjs/operators";
import {JobService} from "../../../services/job.service";
import {NgbTypeaheadSelectItemEvent} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-joblink',
  templateUrl: './joblink.component.html',
  styleUrls: ['./joblink.component.scss']
})
export class JoblinkComponent extends FormComponentBase implements OnInit {
  form: FormGroup;
  @Input() jobId: number;
  @Output() jobSelection =  new EventEmitter<JobNameAndId>();

  searching: boolean;

  constructor(fb: FormBuilder, private jobService: JobService) {
    super(fb);
  }

  ngOnInit(): void {

    this.form = this.fb.group({
      sfJoblink: [null],
    });
  }

  doJobSearch(text$: Observable<string>): Observable<JobNameAndId[]> {
    return text$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      tap(() => {
        this.searching = true;
      }),
      switchMap(text =>
        //todo need call that just returns ShortJob's
        this.jobService.searchPaged({keyword: text, pageSize: 10}).pipe(
          map(result => result.content),
          catchError(() => {
            return of([]);
          }))
      ),
      tap(() => this.searching = false)
    );
  }

  renderJobRow(job: JobNameAndId) {
    return job.name;
  }

  selectSearchResult($event: NgbTypeaheadSelectItemEvent<any>, input: HTMLInputElement) {
      $event.preventDefault();
      const job: JobNameAndId = $event.item;
      input.value = this.renderJobRow(job)
      this.jobSelection.emit(job);
  }
}

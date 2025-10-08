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

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Job} from "../../../../../model/job";
import {AbstractControl, UntypedFormBuilder} from "@angular/forms";
import {JobService} from "../../../../../services/job.service";
import {AutoSaveComponentBase} from "../../../../util/autosave/AutoSaveComponentBase";

@Component({
  selector: 'app-view-job-summary',
  templateUrl: './view-job-summary.component.html',
  styleUrls: ['./view-job-summary.component.scss']
})
export class ViewJobSummaryComponent extends AutoSaveComponentBase implements OnInit, OnChanges {
  @Input() job: Job;
  @Input() editable: boolean;
  @Input() nRows: number = 3;
  isEditing = false;

  constructor(private fb: UntypedFormBuilder,
              private jobService: JobService) {
    super (null) }

  ngOnInit(): void {
    this.form = this.fb.group({
      jobSummary: [this.job.jobSummary],
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    const change = changes.job;
    if (change && change.previousValue !== change.currentValue) {
      //The very first change is fired before ngOnInt has been called.
      //So don't update form if it is not there yet.
      if (this.form) {
        this.jobSummaryControl.patchValue(this.job.jobSummary, {emitEvent: false})
      }
    }
  }

  get jobSummaryControl(): AbstractControl {
    return this.form.get('jobSummary');
  }

  cancelChanges() {
    this.jobSummaryControl.patchValue(this.job.jobSummary);
    this.jobSummaryControl.markAsPristine();
  }

  doSave(formValue: any) {
    this.error = null;
    this.saving = true;

    const summary = this.form.value.jobSummary;
    //Update the local job object.
    this.job.jobSummary = summary;
    //And save the change on the server as well.
    return this.jobService.updateSummary(this.job.id, summary);
  }

  onSuccessfulSave() {
    this.jobSummaryControl.markAsPristine();
    this.saving = false;
  }

  toggleEdit() {
    this.isEditing = !this.isEditing;
  }
}

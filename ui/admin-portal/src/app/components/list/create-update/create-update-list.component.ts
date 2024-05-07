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

import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {SavedList, UpdateSavedListInfoRequest} from '../../../model/saved-list';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {SavedListService} from '../../../services/saved-list.service';
import {SalesforceService} from "../../../services/salesforce.service";
import {JobNameAndId} from "../../../model/job";

@Component({
  selector: 'app-create-update-list',
  templateUrl: './create-update-list.component.html',
  styleUrls: ['./create-update-list.component.scss']
})
export class CreateUpdateListComponent implements OnInit {
  error = null;
  form: FormGroup;
  jobName: string;
  jobId: number;
  saving: boolean;
  savedList: SavedList;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              public salesforceService: SalesforceService,
              private savedListService: SavedListService) {
  }

  ngOnInit() {
    this.form = this.fb.group({
      name: [this.savedList?.name, Validators.required],
      fixed: [this.savedList?.fixed],
    });
  }

  get create(): boolean {
    return !this.savedList;
  }

  get title(): string {
    return this.create ? "Create new candidate list"
      : "Update existing candidate list";
  }

  get isSubmissionList(): boolean {
    return this.savedList && this.savedList.registeredJob;
  }

  get fixedControl() { return this.form.get('fixed'); }
  get nameControl() { return this.form.get('name'); }

  get fixed() { return this.form.value.fixed; }
  get name() { return this.form.value.name; }

  save() {
    this.saving = true;

    const request: UpdateSavedListInfoRequest = {
      name: this.name,
      fixed: this.fixed,
      jobId: this.jobId
    };
    if (this.create) {
      this.savedListService.create(request).subscribe(
        (savedList) => {
          this.closeModal(savedList);
          this.saving = false;
        },
        (error) => {
          this.error = error;
          this.saving = false;
        });
    } else {
      this.savedListService.update(this.savedList.id, request).subscribe(
        (savedList) => {
          this.closeModal(savedList);
          this.saving = false;
        },
        (error) => {
          this.error = error;
          this.saving = false;
        });
    }
  }

  closeModal(savedList: SavedList) {
    this.activeModal.close(savedList);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  onJobSelection(job: JobNameAndId) {
    this.jobName = job.name;
    this.jobId = job.id;

    //If existing name is empty, auto copy into them
    if (!this.nameControl.value) {
      this.nameControl.patchValue(this.jobName);
    }
  }

}

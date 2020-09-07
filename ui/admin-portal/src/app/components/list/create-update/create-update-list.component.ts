/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {
  CreateSavedListRequest,
  SavedList,
  UpdateSavedListInfoRequest
} from "../../../model/saved-list";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedListService} from "../../../services/saved-list.service";
import {JoblinkValidationEvent} from "../../util/joblink/joblink.component";

@Component({
  selector: 'app-create-update-list',
  templateUrl: './create-update-list.component.html',
  styleUrls: ['./create-update-list.component.scss']
})
export class CreateUpdateListComponent implements OnInit {
  error = null;
  form: FormGroup;
  jobName: string;
  saving: boolean;
  savedList: SavedList;
  sfJoblink: string;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
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
    return this.create ? "Make New Saved List"
      : "Update Existing Saved List";
  }

  get fixedControl() { return this.form.get('fixed'); }
  get nameControl() { return this.form.get('name'); }

  get fixed() { return this.form.value.fixed; }
  get name() { return this.form.value.name; }

  save() {
    this.saving = true;

    if (this.create) {
      const request: CreateSavedListRequest = {
        name: this.name,
        fixed: this.fixed,
        sfJoblink: this.sfJoblink
      };
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
      const request: UpdateSavedListInfoRequest = {
        name: this.name,
        fixed: this.fixed,
        sfJoblink: this.sfJoblink
      };
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

  onJoblinkValidation(jobOpportunity: JoblinkValidationEvent) {
    if (jobOpportunity.valid) {
      this.sfJoblink = jobOpportunity.sfJoblink;
      this.jobName = jobOpportunity.jobname;

      //If existing name is empty, auto copy into them
      if (!this.nameControl.value) {
        this.nameControl.patchValue(this.jobName);
      }
    } else {
      this.sfJoblink = null;
      this.jobName = null;
    }
  }

}

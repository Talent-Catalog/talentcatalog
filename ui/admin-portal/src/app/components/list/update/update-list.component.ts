/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {SavedList, UpdateSavedListInfoRequest} from "../../../model/saved-list";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedListService} from "../../../services/saved-list.service";
import {salesforceUrlPattern} from "../../../model/base";

@Component({
  selector: 'app-update-list',
  templateUrl: './update-list.component.html',
  styleUrls: ['./update-list.component.scss']
})
export class UpdateListComponent implements OnInit {
  form: FormGroup;
  error;
  saving: boolean;
  savedList: SavedList;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private savedListService: SavedListService) {
  }

  ngOnInit() {
    this.form = this.fb.group({
      name: [this.savedList.name, Validators.required],
      fixed: [this.savedList.fixed],
      sfJoblink: [this.savedList.sfJoblink, Validators.pattern(salesforceUrlPattern)],
    });
  }

  get fixed() { return this.form.get('fixed'); }
  get name() { return this.form.get('name'); }
  get sfJoblink() { return this.form.get('sfJoblink'); }

  get fixedValue() { return this.form.value.fixed; }
  get nameValue() { return this.form.value.name; }
  get sfJoblinkValue() { return this.form.value.sfJoblink; }

  save() {
    this.saving = true;

    const request: UpdateSavedListInfoRequest = {
      name: this.nameValue,
      fixed: this.fixedValue,
      sfJoblink: this.sfJoblinkValue
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

  closeModal(savedList: SavedList) {
    this.activeModal.close(savedList);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}

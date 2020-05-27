/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {SavedList, UpdateSavedListInfoRequest} from "../../../model/saved-list";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedListService} from "../../../services/saved-list.service";

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

  get name() { return this.form.get('name'); }

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private savedListService: SavedListService) {
  }

  ngOnInit() {
    this.form = this.fb.group({
      name: [null, Validators.required],
    });
  }

  save() {
    this.saving = true;

    const request: UpdateSavedListInfoRequest = {
      name: this.form.value.name
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

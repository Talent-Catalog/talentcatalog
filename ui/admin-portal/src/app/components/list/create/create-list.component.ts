/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CreateSavedListRequest, SavedList} from "../../../model/saved-list";
import {SavedListService} from "../../../services/saved-list.service";

@Component({
  selector: 'app-create-list',
  templateUrl: './create-list.component.html',
  styleUrls: ['./create-list.component.scss']
})
export class CreateListComponent implements OnInit {

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

    const request: CreateSavedListRequest = {
      name: this.form.value.name,
      fixed: false
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
  }

  closeModal(savedList: SavedList) {
    this.activeModal.close(savedList);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}

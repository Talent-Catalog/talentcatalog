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
import {HasName} from "../../../model/base";
import {FormBuilder, FormGroup} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

/**
 * Modal pop up which allows selection of a single item from an array of any
 * objects that have a "name" attribute.
 *
 * The list of names will be presented to the user in a drop down.
 *
 * The component returns the object with the selected name.
 */
@Component({
  selector: 'app-has-name-selector',
  templateUrl: './has-name-selector.component.html',
  styleUrls: ['./has-name-selector.component.scss']
})
export class HasNameSelectorComponent implements OnInit {
  cancel: string = "Cancel";
  form: FormGroup;
  hasNames: HasName[] = [];
  label: string = "Label"
  select: string = "Select";

  constructor(
    private activeModal: NgbActiveModal,
    private fb: FormBuilder) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      hasNameIndex: [],
    });
  }

  onCancel() {
    this.activeModal.dismiss();
  }

  onSelect() {
    const selection = this.form.value.hasNameIndex != null ?
      this.hasNames[this.form.value.hasNameIndex] : null;
    this.activeModal.close(selection);
  }
}

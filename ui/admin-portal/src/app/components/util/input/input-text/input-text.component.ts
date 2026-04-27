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

import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";

@Component({
  selector: 'app-input-text',
  templateUrl: './input-text.component.html',
  styleUrls: ['./input-text.component.scss']
})
export class InputTextComponent implements OnInit {
  title: string = "Input Text";
  initialText = "";
  message: string;
  nRows = 1;
  showCancel: boolean = true;

  form: UntypedFormGroup;

  constructor(
    private activeModal: NgbActiveModal,
    private fb: UntypedFormBuilder) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      text: [this.initialText],
    });
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  close() {
    this.activeModal.close(this.form.value.text);
  }

}

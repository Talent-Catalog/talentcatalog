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
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

export interface UpdateLinkRequest {
  name?: string,
  url?: string
}

@Component({
  selector: 'app-input-link',
  templateUrl: './input-link.component.html',
  styleUrls: ['./input-link.component.scss']
})
export class InputLinkComponent implements OnInit {
  form: UntypedFormGroup;
  initialValue: UpdateLinkRequest;
  instructions: string;
  showCancel: boolean = true;
  title: string = "Input Text";

  constructor(
    private activeModal: NgbActiveModal,
    private fb: UntypedFormBuilder) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      name: [this.initialValue?.name],
      url: [this.initialValue?.url],
    });
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  close() {
    const val: UpdateLinkRequest = {
      name: this.form.value.name,
      url: this.form.value.url
    }
    this.activeModal.close(val);
  }

}

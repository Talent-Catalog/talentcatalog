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
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UserService} from '../../../services/user.service';
import {User} from '../../../model/user';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss']
})
export class ChangePasswordComponent implements OnInit {

  user: User;

  loading: boolean;
  reset: boolean;
  error: any;
  tokenInvalid: boolean;
  form: UntypedFormGroup;
  updated: boolean;

  constructor(private fb: UntypedFormBuilder,
              private activeModal: NgbActiveModal,
              private userService: UserService) {
  }

  ngOnInit() {
    this.loading = false;
    this.reset = false;
    this.error = null;
    this.updated = false;
    this.tokenInvalid = false;
    this.form = this.fb.group({
        password: ['', Validators.required],
        passwordConfirmation: ['', Validators.required]
    });
  }

  updatePassword() {
    this.updated = false;
    this.error = null;
    this.userService.updatePassword(this.user.id, this.form.value).subscribe(
      () => {
        this.closeModal(this.user);
        this.updated = true;
      },
      (error) => {
        this.error = error;
      }
    );
  }

  closeModal(user: User) {
    this.activeModal.close(user);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}

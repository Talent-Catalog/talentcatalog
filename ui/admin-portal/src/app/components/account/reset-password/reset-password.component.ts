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
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {UserService} from "../../../services/user.service";
import {SendResetPasswordEmailRequest} from "../../../model/candidate";
import {environment} from "../../../../environments/environment";
import {EMAIL_REGEX} from "../../../model/base";

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {

  loading: boolean;
  error: any;
  resetPasswordForm: UntypedFormGroup;
  updated: boolean;

  backgroundImage: string;

  readonly emailRegex: string = EMAIL_REGEX;

  constructor(private fb: UntypedFormBuilder,
              private userService: UserService) {
  }

  ngOnInit(): void {
    this.backgroundImage = `url(${environment.assetBaseUrl}/assets/images/login-splash-v2.2.1.png)`;
    this.loading = false;
    this.error = null;
    this.updated = false;
    this.resetPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  get email(): string {
    return this.resetPasswordForm.value.email;
  }

  resetForm() {
    this.resetPasswordForm.patchValue({
      email: '',
    });
    const keys: string[] = Object.keys(this.resetPasswordForm.controls);
    for (const key of keys) {
      this.resetPasswordForm.controls[key].markAsPristine();
    }
  }

  sendResetEmail() {
    this.updated = false;
    this.error = null;

    const req: SendResetPasswordEmailRequest = new SendResetPasswordEmailRequest();
    req.email = this.email;

    this.userService.sendResetPassword(req).subscribe(
        () => {
          this.resetForm();
          this.updated = true;
        },
        (error) => {
          this.error = error;
        }
    );


  }

}

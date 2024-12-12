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
import {UserService} from '../../../services/user.service';
import {SendResetPasswordEmailRequest} from "../../../model/candidate";

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {

  loading: boolean;
  error: any;
  form: UntypedFormGroup;
  updated: boolean;

  constructor(private fb: UntypedFormBuilder,
              private userService: UserService) {
  }

  ngOnInit() {
    this.loading = false;
    this.error = null;
    this.updated = false;
    this.form = this.fb.group({
      email: ['', Validators.required]
    });
  }

  get email(): string {
    return this.form.value.email;
  }

  resetForm() {
    this.form.patchValue({
      email: '',
    });
    const keys: string[] = Object.keys(this.form.controls);
    for (const key of keys) {
      this.form.controls[key].markAsPristine();
    }
  }

  sendResetEmail() {
    this.updated = false;
    this.error = null;

    // const action = 'resetPassword';
    // this.reCaptchaV3Service.execute(action).subscribe(
    //   (token) => this.sendResetWithToken(token),
    //   (error) => {
    //     console.log(error);
    //   }
    // );
    this.sendResetWithToken(null);
  }

  private sendResetWithToken(token: string) {
    const req: SendResetPasswordEmailRequest = new SendResetPasswordEmailRequest();
    req.email = this.email;
    req.reCaptchaV3Token = token;

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

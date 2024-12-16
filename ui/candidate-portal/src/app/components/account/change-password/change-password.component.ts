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
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss']
})
export class ChangePasswordComponent implements OnInit {

  loading: boolean;
  reset: boolean;
  error: any;
  tokenInvalid: boolean;
  form: UntypedFormGroup;
  updated: boolean;
  token: string;

  constructor(private fb: UntypedFormBuilder,
              private userService: UserService,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.loading = false;
    this.reset = false;
    this.error = null;
    this.updated = false;
    this.tokenInvalid = false;

    this.route.paramMap
      .subscribe(params => {
        this.token = params.get('token');
        if (this.token) {
          const request = {
            token: this.token
          };

          this.userService.checkPasswordResetToken(request).subscribe(() => {
              this.loading = false;
              this.reset = true;
              this.form = this.fb.group({
                 token: [this.token, Validators.required],
                 password: ['', Validators.required],
                 passwordConfirmation: ['', Validators.required]
              });
            },
            error => {
              this.error = error;
              this.tokenInvalid = true;
            });
        } else {
           this.form = this.fb.group({
              oldPassword: ['', Validators.required],
              password: ['', Validators.required],
              passwordConfirmation: ['', Validators.required]
           });
        }
      });
  }

  updatePassword() {
    this.updated = false;
    this.error = null;
    if (this.reset) {
        this.userService.resetPassword(this.form.value).subscribe(
          () => {
            setTimeout(() => {
              this.router.navigate(['/login']);
            }, 2000);
            this.resetForm();
            this.updated = true;
          },
          (error) => {
            this.error = error;
          }
        );
    } else {
        this.userService.updatePassword(this.form.value).subscribe(
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

  resetForm() {
    if (this.reset) {
        this.form.patchValue({
          token: '',
          password: '',
          passwordConfirmation: '',
        });    } else {
        this.form.patchValue({
          oldPassword: '',
          password: '',
          passwordConfirmation: '',
        });
    }
    const keys: string[] = Object.keys(this.form.controls);
    for (const key of keys) {
      this.form.controls[key].markAsPristine();
    }
  }

}

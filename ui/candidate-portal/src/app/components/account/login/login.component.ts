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
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {AuthService} from "../../../services/auth.service";
import {ActivatedRoute, Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {LoginRequest} from "../../../model/base";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm: FormGroup;
  loading: boolean;
  returnUrl: string;
  error;

  constructor(private builder: FormBuilder,
              private authService: AuthService,
              private authenticationService: AuthenticationService,
              private candidateService: CandidateService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'] || '/home';
    });

    this.loginForm = this.builder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    })
  }

  get username(): string {
    return this.loginForm.value.username;
  }

  get password(): string {
    return this.loginForm.value.password;
  }

  login() {
    this.error = null;
    if (this.loginForm.invalid) {
      return;
    }
    if (this.loading) { return; }
    this.loading = true;

    this.loginWithToken(null);
  }

  private loginWithToken(token: string) {
    const req: LoginRequest = new LoginRequest();
    req.username = this.username;
    req.password = this.password;
    req.reCaptchaV3Token = token;

    this.authenticationService.login(req)
      .subscribe(() => {
        this.loading = false;
        // Get candidate number to save in storage to display in the header
        this.candidateService.getCandidateNumber().subscribe(
          (candidate) => {
            this.candidateService.setCandNumberStorage(candidate.candidateNumber);
          }
        )
        this.router.navigateByUrl(this.returnUrl);
      }, error => {
        console.log(error);
        this.error = error;
        this.loading = false;
      });

  }
}


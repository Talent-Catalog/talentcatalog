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
import {AuthService} from "../../../services/auth.service";
import {ActivatedRoute, Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {LoginRequest} from "../../../model/base";
import {ChangePasswordComponent} from '../change-password/change-password.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {CandidateStatus} from "../../../model/candidate";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm: UntypedFormGroup;
  loading: boolean;
  returnUrl: string;
  error;

  constructor(private builder: UntypedFormBuilder,
              private authService: AuthService,
              private authenticationService: AuthenticationService,
              private candidateService: CandidateService,
              private route: ActivatedRoute,
              private router: Router,
              private modalService: NgbModal) {
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
        this.candidateService.getCandidatePersonal().subscribe(
          (candidate) => {
            if (candidate.changePassword  === true) {
              this.modalService.open(ChangePasswordComponent, {
                centered: true
              });
            }
            this.candidateService.setCandNumberStorage(candidate.candidateNumber);
          }
        )

        // Get candidate status
        this.candidateService.getCandidatePersonal().subscribe(
          (candidate) => {
            this.authenticationService.setCandidateStatus(CandidateStatus[candidate.status]);
          }
        )

        //todo If terms out of date, replace return url with /home

        this.router.navigateByUrl(this.returnUrl);
      }, error => {
        console.log(error);
        this.error = error;
        this.loading = false;
      });

  }
}


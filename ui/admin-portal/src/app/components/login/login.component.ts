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
import {AuthService} from "../../services/auth.service";
import {ActivatedRoute, Router} from "@angular/router";
import {LoginRequest} from "../../model/base";
import {ReCaptchaV3Service} from "ng-recaptcha";
import {User} from "../../model/user";
import {EncodedQrImage} from "../../util/qr";
import {ShowQrCodeComponent} from "../util/qr/show-qr-code/show-qr-code.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

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
              private modalService: NgbModal,
              private reCaptchaV3Service: ReCaptchaV3Service,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'] || '';
    });

    this.loginForm = this.builder.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
      totpToken: ['']
    })
  }

  get username(): string {
    return this.loginForm.value.username;
  }

  get password(): string {
    return this.loginForm.value.password;
  }

  get totpToken(): string {
    return this.loginForm.value.totpToken;
  }

  login() {
    this.error = null;
    if (this.loginForm.invalid) {
      return;
    }
    if (this.loading) { return; }
    this.loading = true;

    const action = 'login';
    this.reCaptchaV3Service.execute(action).subscribe(
      (token) => this.loginWithToken(token),
      (error) => {
        console.log(error);
      }
    );
  }

  private loginWithToken(token: string) {
    const req: LoginRequest = new LoginRequest();
    req.username = this.username;
    req.password = this.password;
    req.totpToken = this.totpToken;
    req.reCaptchaV3Token = token;

    this.authService.login(req)
      .subscribe(() => {
        this.loading = false;
        this.checkMfaSetup();
      }, error => {
        // console.log(error);
        this.error = error;
        this.loading = false;
      });

  }

  private checkMfaSetup() {
    const user: User = this.authService.getLoggedInUser();
    if (!user.usingMfa || user.mfaConfigured) {
      this.router.navigateByUrl(this.returnUrl);
    } else {
      //User needs to configure mfa before proceeding further.
      this.mfaSetup();
    }
  }

  mfaSetup() {
    this.authService.mfaSetup().subscribe(
      (qr: EncodedQrImage) => { this.showQrCode(qr)}
    )
  }

  private showQrCode(qr: EncodedQrImage) {
    const modal = this.modalService.open(ShowQrCodeComponent, { backdrop: 'static' });
    modal.componentInstance.qr = qr;
    modal.result
    .then(() => {
      this.router.navigateByUrl(this.returnUrl);
    })
    .catch(() => {
      this.router.navigateByUrl(this.returnUrl);
    });
  }
}


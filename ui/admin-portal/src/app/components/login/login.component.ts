/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, OnDestroy, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {User} from "../../model/user";
import {EncodedQrImage} from "../../util/qr";
import {ShowQrCodeComponent} from "../util/qr/show-qr-code/show-qr-code.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthenticationService} from "../../services/authentication.service";
import {environment} from "../../../environments/environment";
import {PartnerService} from "../../services/partner.service";
import {AuthorizationService} from "../../services/authorization.service";
import {IdpStatus} from "../../services/idp-status";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit, OnDestroy {
  authStatus: IdpStatus;
  private authStatusSub?: Subscription;

  loginForm: UntypedFormGroup;
  loading: boolean;
  returnUrl: string;
  error;

  backgroundImage: string;
  loginImage: string;

  constructor(private builder: UntypedFormBuilder,
              private authenticationService: AuthenticationService,
              private authorizationService: AuthorizationService,
              private modalService: NgbModal,
              private route: ActivatedRoute,
              private partnerService: PartnerService,
              private router: Router) {
  }

  ngOnInit() {
    this.backgroundImage = `url(${environment.assetBaseUrl}/assets/images/login-splash-v2.2.1.png)`;
    this.loginImage = `${environment.assetBaseUrl}/assets/images/tcHorizontalLogo.png`;

    this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'] || '';
    });

    this.loginForm = this.builder.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
      totpToken: ['']
    })

    this.authStatusSub = this.authenticationService.getAuthStatus().subscribe(
      status => this.authStatus = status);

    const authAction = this.route.snapshot.queryParamMap.get('authAction');

    if (this.authenticationService.isAuthenticated()) {
      this.authenticationService.clearAuthError();
      if (authAction === 'register') {
        console.log("Shouldn't happen to register");
        // let request: OauthRegistrationRequest = {
        //   //todo These consents are being mocked for now. When new UI is designed
        //   //the register button should be disabled until the user has consented to the terms.
        //   contactConsentRegistration: true,
        //   contactConsentPartners: true
        // }
        // this.completeRegister(request);
      } else if (authAction === 'login') {
        this.completeLogin();
      }
    }

  }

  ngOnDestroy(): any {
    this.authStatusSub?.unsubscribe();
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

  private checkMfaSetup() {
    const user: User = this.authenticationService.getLoggedInUser();
    if (!user.usingMfa || user.mfaConfigured) {
      this.router.navigateByUrl(this.returnUrl);
    } else {
      //User needs to configure mfa before proceeding further.
      this.mfaSetup();
    }
  }

  mfaSetup() {
    this.authenticationService.mfaSetup().subscribe(
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

  private checkMfaAndDpa() {
    if (!this.authorizationService.isSourcePartner()) {
      // Non-partner users skip DPA check and proceed to MFA
      this.checkMfaSetup();
      return;
    }
    this.partnerService.requiresDpaAcceptance().subscribe({
      next: (requiresDpa: boolean) => {
        if (requiresDpa) {
          this.router.navigateByUrl('/dpa');
        } else {
          this.checkMfaSetup();
        }
      },
      error: error => {
        this.error = error;
        this.loading = false;
      }
    });
  }

  login() {
    this.authenticationService.login();
  }

  completeLogin() {
    this.error = null;
    this.authenticationService.completeLogin().subscribe({
      next: (response) => {
        this.router.navigate(['/home']);
      },
      error: (error) => {
        //Display error
        this.error = error;
        //Log out the user if the login did not complete successfully.
        this.authenticationService.logout();
      }
    })
  }

}


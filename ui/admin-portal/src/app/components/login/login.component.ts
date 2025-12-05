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
import {ActivatedRoute, Router} from "@angular/router";
import {DtoType, LoginRequest} from "../../model/base";
import {User} from "../../model/user";
import {EncodedQrImage} from "../../util/qr";
import {ShowQrCodeComponent} from "../util/qr/show-qr-code/show-qr-code.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthenticationService} from "../../services/authentication.service";
import {environment} from "../../../environments/environment";
import {Partner} from "../../model/partner";
import {TermsInfoDto, TermsType} from "../../model/terms-info-dto";
import {forkJoin} from "rxjs";
import {TermsInfoService} from "../../services/terms-info.service";
import {PartnerService} from "../../services/partner.service";
import {AuthorizationService} from "../../services/authorization.service";

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

  backgroundImage: string;
  loginImage: string;

  constructor(private builder: UntypedFormBuilder,
              private authenticationService: AuthenticationService,
              private authorizationService: AuthorizationService,
              private modalService: NgbModal,
              private route: ActivatedRoute,
              private partnerService: PartnerService,
              private termsInfoService: TermsInfoService,
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

    // const action = 'login';
    // this.reCaptchaV3Service.execute(action).subscribe(
    //   (token) => this.loginWithToken(token),
    //   (error) => {
    //     console.log(error);
    //   }
    // );
    this.loginWithToken(null);
  }

  private loginWithToken(token: string) {
    const req: LoginRequest = new LoginRequest();
    req.username = this.username;
    req.password = this.password;
    req.totpToken = this.totpToken;
    req.reCaptchaV3Token = token;

    this.authenticationService.login(req)
    .subscribe(() => {
      this.loading = false;
      this.checkMfaAndDpa();
    }, error => {
      // console.log(error);
      this.error = error;
      this.loading = false;
    });

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
    const user: User = this.authenticationService.getLoggedInUser();
    // Check if user is a source partner (based on role or partner association)
    if (this.authorizationService.isSourcePartner()) {
      // Fetch current DPA and partner info
      forkJoin({
        'currentDpa': this.termsInfoService.getCurrentByType(TermsType.DATA_PROCESSING_AGREEMENT),
        'partner': this.partnerService.getPartner(user.partner.id, DtoType.MINIMAL)
      }).subscribe(
        results => {
          this.checkDpaStatus(results.partner, results.currentDpa);
        },
        error => {
          this.error = error;
          this.loading = false;
        }
      );
    } else {
      // Non-partner users skip DPA check and proceed to MFA
      this.checkMfaSetup();
    }
  }

  private checkDpaStatus(partner: Partner, currentDpa: TermsInfoDto) {
    // Check if partner has accepted the latest DPA
    if (partner.sourcePartner && !partner.acceptedDataProcessingAgreementId) {
      // Redirect to DPA acceptance page, ignoring returnUrl
      this.router.navigateByUrl('/dpa');
    } else {
      // Proceed to MFA check if DPA is accepted
      this.checkMfaSetup();
    }
  }
}


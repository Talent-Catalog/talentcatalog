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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {UntypedFormBuilder, UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router, UrlTree} from "@angular/router";
import {BrandingService} from "../../../services/branding.service";
import {CandidateService} from "../../../services/candidate.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {RegistrationService} from "../../../services/registration.service";
import {LanguageService} from "../../../services/language.service";
import {RegisterCandidateRequest} from "../../../model/candidate";
import {EMAIL_REGEX} from "../../../model/base";
import {
  CompleteOauthAuthenticationRequest
} from "../../../model/complete-oauth-authentication-request";
import {finalize} from "rxjs/internal/operators";
import {timer} from "rxjs";

@Component({
  selector: 'app-registration-create-account',
  templateUrl: './registration-create-account.component.html',
  styleUrls: ['./registration-create-account.component.scss']
})
export class RegistrationCreateAccountComponent implements OnInit {

  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  /**
   * This corresponds to the query parameter 'authAction' which is set by the IdpService
   *  and indicates whether the user is logging in or registering.
   *  <p/>
   *  It is used to direct the user to the correct component (RegisterComponent or HomeComponent)
   *  depending on whether it is a login or register action.
   *  <p/>
   *  When this component is entered with a non-null authAction, the login and register buttons
   *  are hidden and the component completes the action by calling down to the server.
   */
  authAction: string | null;

  consented: boolean = false;

  currentUrlAsTree: UrlTree;

  registrationForm: UntypedFormGroup;
  error: any;

  // Form states
  loading: boolean;
  saving: boolean;

  authenticated: boolean;

  usAfghan: boolean;
  partnerName: string;

  readonly emailRegex: string = EMAIL_REGEX;

  constructor(private builder: UntypedFormBuilder,
              private router: Router,
              private route: ActivatedRoute,
              private brandingService: BrandingService,
              private candidateService: CandidateService,
              private authenticationService: AuthenticationService,
              private registrationService: RegistrationService,
              private languageService: LanguageService) { }

  ngOnInit(): void {
    this.authenticated = false;
    this.loading = true;

    this.currentUrlAsTree = this.router.parseUrl(this.router.url);

    this.registrationForm = this.builder.group({
      username: [''],
      password: [''],
      passwordConfirmation: ['']
    });

    if (this.authenticationService.isAuthenticated()) {
      this.authAction = this.route.snapshot.queryParamMap.get(AuthenticationService.CALLBACK_ACTION_PARAM_NAME);
      this.authenticationService.clearAuthError();
      if (!this.authAction) {
        //Just continue with registration if there is no auth action -
        //this can happen if the user is already authenticated when they start the
        //registration process.
        this.registrationService.next();
      } else if (this.authAction === AuthenticationService.REGISTER_ACTION) {
        let request: CompleteOauthAuthenticationRequest = {
          //todo These consents are being mocked for now. When new UI is designed
          //the register button should be disabled until the user has consented to the terms.
          contactConsentRegistration: true,
          contactConsentPartners: true
        }
        this.completeRegister(request);
      } else {
        this.error = 'Unknown or unexpected auth action: ' + this.authAction;
        console.error(this.error);
      }
    } else {
      this.authenticated = false;
      this.authAction = null;
    }


    // Get the partner name from the branding info object.
    this.brandingService.getBrandingInfo().subscribe((brandingInfo) => this.partnerName = brandingInfo.partnerName)

    // The user has not registered - add the email consent fields
    this.registrationForm.addControl('contactConsentRegistration', new UntypedFormControl(false, [Validators.requiredTrue]));
    this.registrationForm.addControl('contactConsentPartners', new UntypedFormControl(false));

    this.loading = false;
  }

  onToggleConsent() {
    this.consented = !this.consented;
  }

  onRegister() {
    //Logout Idp if it thinks we are still logged in. Can happen.
    this.authenticationService.logoutIdp();
    this.authenticationService.register(
      this.computeRedirectUri(
        AuthenticationService.REGISTER_ACTION), this.languageService.getSelectedLanguage());
  }


  //todo This should only be called once consent has been gathered.
  //todo Also the request should contain all the utm parameters see getParamesAndRegister() below.
  completeRegister(request: CompleteOauthAuthenticationRequest) {
    this.error = null;
    this.loading = true;
    this.authenticationService.completeRegister(request)
    .pipe(finalize(() => this.loading = false))
    .subscribe({
      next: (response) => {
        //Proceed with registration.
        this.registrationService.next();
      },
      error: (error) => {
        //Display error
        this.error = error;
        this.pauseThenLogout();
      }
    })
  }

  private pauseThenLogout() {
    //Log out the user if the login did not complete successfully.
    //Pause so user can see the error before logging out and being redirected to the landing page.
    timer(10000).subscribe(() => {
      //Log out the user if the registration did not complete successfully.
      this.authenticationService.logout();
    });
  }

  private computeRedirectUri(action: string) {
    const urlTree = this.currentUrlAsTree;
    urlTree.queryParams[AuthenticationService.CALLBACK_ACTION_PARAM_NAME] = action;
    return urlTree.toString();
  }

  private getParamsAndRegister() {
    const params = this.route.snapshot.queryParamMap;

    // Check for the partner query param and use it to configure the branding service.
    const req: RegisterCandidateRequest = new RegisterCandidateRequest();
    req.partnerAbbreviation = this.brandingService.partnerAbbreviation;

    //Populate query params
    if (params.has('r')) {
      req.referrerParam = params.get('r');
    }
    if (params.has('utm_source')) {
      req.utmSource = params.get('utm_source');
    }
    if (params.has('utm_campaign')) {
      req.utmCampaign = params.get('utm_campaign');
    }
    if (params.has('utm_medium')) {
      req.utmMedium = params.get('utm_medium');
    }
    if (params.has('utm_content')) {
      req.utmContent = params.get('utm_content');
    }
    if (params.has('utm_term')) {
      req.utmContent = params.get('utm_term');
    }

    //Populate contact consent
    req.contactConsentRegistration = this.registrationForm.value.contactConsentRegistration;
    req.contactConsentPartners = this.registrationForm.value.contactConsentPartners;

    // this.authenticationService.register(req).subscribe(
    //   (response) => {
    //     // If successfully registered, check if US-Afghan and if so update the survey.
    //     if (this.usAfghan) {
    //       // Set special value of candidate survey type indicating US Afghan
    //       const request: UpdateCandidateSurvey = {
    //         surveyTypeId: US_AFGHAN_SURVEY_TYPE,
    //       }
    //       this.candidateService.updateCandidateSurvey(request).subscribe(
    //         (res) => {
    //           this.saving = false;
    //         }, (error) => {
    //           this.error = error;
    //           this.saving = false;
    //         }
    //       )
    //     }
    //     this.registrationService.next();
    //   },
    //   (error) => {
    //     this.error = error;
    //     this.saving = false;
    //   }
    // );
  }
}

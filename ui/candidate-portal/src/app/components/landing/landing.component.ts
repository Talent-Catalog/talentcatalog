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
import {ActivatedRoute, Router, UrlTree} from '@angular/router';
import {LanguageService} from '../../services/language.service';
import {initializePhraseAppEditor} from "ngx-translate-phraseapp";
import {BrandingInfo, BrandingService} from "../../services/branding.service";
import {AuthenticationService} from "../../services/authentication.service";
import {Subscription} from "rxjs";
import {IdpStatus} from "../../services/idp-status";
import {finalize} from "rxjs/internal/operators";
import {
  CompleteOauthAuthenticationRequest
} from "../../model/complete-oauth-authentication-request";
import {RegistrationService} from "../../services/registration.service";

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})

/**
 * This is where the application starts if there is no extra path in the url.
 * This is specified in the AppRoutingModel when the url path is empty ('').
 * <p/>
 * The ngInit method will determine whether the user is already authenticated...
 * <p>
 *  If authenticated - redirected to '/home' - which is serviced by HomeComponent (as defined
 *  in AppRoutingModule).
 * </p>
 * <p>
 *  If not authenticated then processing stays in this component.
 * </p>
 * <p>
 *   If the Registration button is clicked the user is redirected to '/register', serviced by the
 *   RegisterComponent (as defined in AppRoutingModule).
 * </p>
 */
export class LandingComponent implements OnInit, OnDestroy {
  MODE_PARAM_NAME = "mode";
  MODE_LOGIN = "login";
  MODE_REGISTER = "register";

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
  authStatus: IdpStatus;
  private authStatusSub?: Subscription;
  private brandingInfo: BrandingInfo;
  consented: boolean = false;
  currentUrlAsTree: UrlTree;
  error: string;
  loading: boolean;
  private _mode: string | null;
  showUSAfghanInfo: boolean = false;

  constructor(private authenticationService: AuthenticationService,
              private brandingService: BrandingService,
              private registrationService: RegistrationService,
              private router: Router,
              private route: ActivatedRoute,
              private languageService: LanguageService) {
  }

  ngOnInit() {
    this.authStatusSub = this.authenticationService.getAuthStatus().subscribe(
      status => this.authStatus = status);

    this.currentUrlAsTree = this.router.parseUrl(this.router.url);

    const pathMinusQueryParams = this.router.url.split('?')[0];
    if (pathMinusQueryParams.includes('register')) {
      this.mode = this.MODE_REGISTER;
    } else if (pathMinusQueryParams.includes('login')) {
      this.mode = this.MODE_LOGIN;
    } else {
      let modeValue = this.route.snapshot.queryParamMap.get(this.MODE_PARAM_NAME);
      this.mode = modeValue === this.MODE_REGISTER ? this.MODE_REGISTER : this.MODE_LOGIN;
    }
    this.authAction = this.route.snapshot.queryParamMap.get(AuthenticationService.CALLBACK_ACTION_PARAM_NAME);

    if (this.authenticationService.isAuthenticated()) {
      this.authenticationService.clearAuthError();

      //Note that we can't reliably tell whether the user logged to an existing account or
      //registered a new account on the Idp service because, even though they might have clicked
      //on Login in our Angular app, and we directed them to Login on the Idp service, they can
      //switch to registering inside the Idp service. And vice versa: we can direct them to
      //register, and they switch to logging in.
      //All we can rely on is that they are authenticated. We let our server figure out whether
      //it is a new user that we should route to gather registration data, or whether it is an
      //existing user who has just logged in.
      this.completeAuthentication();

      // //todo We can't reliably tell without calling the server whether this is a login or
      // // registration action
      // //Need to call server then figure out what to do. If it is a registration and no consent
      // //has been given (this could happen if did a Login, then switched to Registration in Keycloak)
      // // we need to grab consent before progressing.
      // if (this.authAction === AuthenticationService.REGISTER_ACTION) {
      //   if (this.mode !== this.MODE_REGISTER) {
      //     this.mode = this.MODE_REGISTER;
      //   }
      // } else if (this.authAction === AuthenticationService.LOGIN_ACTION) {
      //   this.completeLogin();
      // }
    }

    //todo All this query param handling needs to happen after we are authenticated
    //Note that we deliberately use a snapshot rather than a subscribe
    //so that we pick up the lang query even if there is a redirect.
    //eg like login redirects to /home if already authenticated.
    //With a subscribe, the query can be lost in the redirect.
    // const lang = this.route.snapshot.queryParams['lang'];
    // //Need to delay changing language otherwise you get ExpressionChangedAfterItHasBeenCheckedError
    // setTimeout(
    //   () => this.languageService.changeLanguage(lang), 1000
    // )
    //
    // //Branding info drives some aspects of this page
    // this.brandingService.getBrandingInfo().subscribe(
    //   (response: BrandingInfo) => this.setBrandingInfo(response),
    //   (error) => console.log(error)
    // );

    /**
     * Look for xlate query parameter which requests "in context" translation.
     * The value of the query parameter is the password which must be validated by the server
     * for "in context" translation to be enabled.
     */
    // const xlate = this.route.snapshot.queryParams['xlate'];
    // if (!xlate) {
    //   //No parameter (or no password supplied) - just continue normally
    //   this.proceed();
    // } else {
    //   //Validate supplied password
    //   this.authenticationService.authenticateInContextTranslation(xlate).subscribe(
    //     () => {
    //       //Password validated, initialize "in context" translation
    //       LandingComponent.intializeInContextTranslation();
    //       //...and proceed
    //       this.proceed();
    //     },
    //     (error) => {
    //       //Password did not validate. Log the error quietly...
    //       console.log(error);
    //       //...and proceed as if no request was made
    //       this.proceed();
    //     }
    //   )
    // }
  }

  ngOnDestroy(): void {
    this.authStatusSub?.unsubscribe();
  }

  get mode(): string | null {
    return this._mode;
  }

  set mode(value: string | null) {
    this._mode = value;
    this.consented = false;
  }

  onLogin() {
    this.authenticationService.login(
      this.computeRedirectUri(AuthenticationService.LOGIN_ACTION), this.languageService.getSelectedLanguage());
  }

  onRegister() {
    //Logout Idp if it thinks we are still logged in. Can happen.
    this.authenticationService.logoutIdp();
    this.authenticationService.register(
      this.computeRedirectUri(
        AuthenticationService.REGISTER_ACTION), this.languageService.getSelectedLanguage());
  }

  onCompleteRegister() {
    this.completeAuthentication();
  }

  onToggleConsent() {
    this.consented = !this.consented;
  }

  private computeRedirectUri(action: string) {
    const urlTree = this.currentUrlAsTree;
    urlTree.queryParams[AuthenticationService.CALLBACK_ACTION_PARAM_NAME] = action;
    return urlTree.toString();
  }

  private completeAuthentication() {
    let request: CompleteOauthAuthenticationRequest = {
      contactConsentRegistration: true,
      contactConsentPartners: true
      //todo query params
    }
    this.error = null;
    this.loading = true;
    this.authenticationService.completeAuthentication(request)
    .pipe(finalize(() => {
      this.authAction = null;
      this.loading = false;
    }))
    .subscribe({
      next: (response) => {
        //The server response tells us whether this is a new user or an existing user.
        if (response.userIsNew) {
          //New users should go through the registration process where we ask them for extra data
          //about their skills, qualifications and experiences.
          if (response.contactConsentRegistration) {
            this.gatherRegistrationData();
          } else {
            //If they haven't already given their consent to contact them, we need to ask them
            //to do that before proceeding further.
            this.requestContactConsent();
          }
        } else {
          this.router.navigate(['/home']);
        }
      },
      error: (error) => {
        //Display error
        this.error = error;
        this.pauseThenLogout();
      }
    })
  }

  gatherRegistrationData() {
    //Proceed with registration.
    this.registrationService.next();
    this.router.navigate(['/profile']);
  }

  private requestContactConsent() {
    //todo Set mode that shows contact consent checkbox plus button to complete Authentication again.
    this.mode = this.MODE_REGISTER;
  }

  completeLogin() {
    this.error = null;
    this.loading = true;
    this.authenticationService.completeLogin()
    .pipe(finalize(() => {
      this.authAction = null;
      this.loading = false;
    }))
    .subscribe({
      next: (response) => {
        this.router.navigate(['/home']);
      },
      error: (error) => {
        //Display error
        this.error = error;
        this.pauseThenLogout();
      }
    })
  }

  //todo This should only be called once consent has been gathered.
  //todo Also the request should contain all the utm parameters see getParamesAndRegister() below.
  private completeRegister(request: CompleteOauthAuthenticationRequest) {
    this.error = null;
    this.loading = true;
    this.authenticationService.completeRegister(request)
    .pipe(finalize(() => {
      this.authAction = null;
      this.loading = false;
    }))
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
    void this.router.navigate(['/logout'], { queryParams: { reason: this.error } });
  }

  dismissAuthError(): void {
    this.authenticationService.clearAuthError();
  }

  private setBrandingInfo(brandingInfo: BrandingInfo) {
    this.brandingInfo = brandingInfo;
    this.showUSAfghanInfo = brandingInfo.websiteUrl?.includes("talentbeyondboundaries.org");
  }

  private proceed() {
    if (this.authenticationService.isAuthenticated()) {
      //todo If this is registration we also want the utm params. See
      //todo getParamsAndRegister in RegistrationCreateAccountComponent.
      //Update the auth profile (this will include idp keys and email)
      // this.authenticationService.completeRegister().subscribe({
      //   next: (response) => {
      //     this.router.navigate(['/home']);
      //   },
      //   error: (error) => {
      //     console.error('Error updating auth profile:', error);
      //     this.authenticationService.clearAuthError();
      //     this.router.navigate(['/home']);
      //   }
      // })
    } else {
      //Logging in or registering
      const usAfghan: boolean = this.route.snapshot.queryParams['source'] === 'us-afghan';
      this.languageService.setUsAfghan(usAfghan);
    }
  }

  private static intializeInContextTranslation() {

    //This is the Phrase "In context" translation configuration.
    //See https://phrase.com/blog/posts/angular-l10n-in-context-translation-editing/
    const config = {
      //This is the id associated with the TalentCatalog project - see Project Settings|API
      projectId: '7043871a7114505fdde77b5e2557331d',
      //Set this true to enable in context translation
      phraseEnabled: true,
      prefix: "{{__",
      suffix: "__}}",
      autoLowercase: false,
      fullReparse: true
    };

    initializePhraseAppEditor(config);
  }
}

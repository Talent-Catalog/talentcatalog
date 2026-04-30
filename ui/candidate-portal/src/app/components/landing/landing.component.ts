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

import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {LanguageService} from '../../services/language.service';
import {initializePhraseAppEditor} from "ngx-translate-phraseapp";
import {BrandingInfo, BrandingService} from "../../services/branding.service";
import {AuthenticationService} from "../../services/authentication.service";
import {Subscription} from "rxjs";
import {AuthStatus} from "../../services/auth-status";
import {OauthRegistrationRequest} from "../../model/oauth-registration-request";

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
 *  If not authenticated then processing stays in this component, which displays the
 *  LoginComponent (app-login) and a Registration button.
 * </p>
 * <p>
 *   If the Registration button is clicked the user is redirected to '/register', serviced by the
 *   RegisterComponent (as defined in AppRoutingModule).
 * </p>
 */
export class LandingComponent implements OnInit, OnDestroy {
  authStatus: AuthStatus;
  private authStatusSub?: Subscription;
  private brandingInfo: BrandingInfo;
  showUSAfghanInfo: boolean = false;

  constructor(private authenticationService: AuthenticationService,
              private brandingService: BrandingService,
              private router: Router,
              private route: ActivatedRoute,
              private languageService: LanguageService) {
  }

  // todo Login and Register should now just be buttons in this page?

  ngOnInit() {
    this.authStatusSub = this.authenticationService.getAuthStatus().subscribe(
      status => this.authStatus = status);

    const authAction = this.route.snapshot.queryParamMap.get('authAction');

    if (this.authenticationService.isAuthenticated()) {
      this.authenticationService.clearAuthError();
      if (authAction === 'register') {
        let request: OauthRegistrationRequest = {

        }
        this.completeRegister(request);
      } else if (authAction === 'login') {
        this.completeLogin();
      }
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

  onLogin() {
    this.authenticationService.login();
  }

  onRegister() {
    this.authenticationService.register();
  }

  completeLogin() {
    this.authenticationService.completeLogin().subscribe({
      next: (response) => {
        this.router.navigate(['/home']);
      },
      error: (error) => {
        console.error('Error completing login:', error);
      }
    })
  }

  completeRegister(request: OauthRegistrationRequest) {
    this.authenticationService.completeRegister(request).subscribe({
      next: (response) => {
        this.router.navigate(['/register']);
      },
      error: (error) => {
        console.error('Error completing registration:', error);
      }
    })
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

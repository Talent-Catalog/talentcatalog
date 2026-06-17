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
import {Subscription, timer} from "rxjs";
import {IdpStatus} from "../../services/idp-status";
import {finalize} from "rxjs/internal/operators";

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
  currentUrlAsTree: UrlTree;
  error: string;
  showUSAfghanInfo: boolean = false;

  constructor(private authenticationService: AuthenticationService,
              private brandingService: BrandingService,
              private router: Router,
              private route: ActivatedRoute,
              private languageService: LanguageService) {
  }

  ngOnInit() {
    this.authStatusSub = this.authenticationService.getAuthStatus().subscribe(
      status => this.authStatus = status);

    this.currentUrlAsTree = this.router.parseUrl(this.router.url);

    if (this.authenticationService.isAuthenticated()) {
      this.authAction = this.route.snapshot.queryParamMap.get(AuthenticationService.CALLBACK_ACTION_PARAM_NAME);
      this.authenticationService.clearAuthError();
      if (this.authAction === AuthenticationService.REGISTER_ACTION) {
        this.redirectToRegisterPage();
      } else if (this.authAction === AuthenticationService.LOGIN_ACTION) {
        this.completeLogin();
      }
    } else {
      this.authAction = null;
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
    this.authenticationService.login(
      this.computeRedirectUri(AuthenticationService.LOGIN_ACTION), this.languageService.getSelectedLanguage());
  }

  /**
   * Just redirect to the register page.
   * The register component will handle the rest.
   */
  onRegister() {
    this.router.navigate(['/register']);
  }

  private computeRedirectUri(action: string) {
    const urlTree = this.currentUrlAsTree;
    urlTree.queryParams[AuthenticationService.CALLBACK_ACTION_PARAM_NAME] = action;
    return urlTree.toString();
  }

  completeLogin() {
    this.error = null;
    this.authenticationService.completeLogin()
    .pipe(finalize(() => this.authAction = null))
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

  redirectToRegisterPage() {
    //todo We have to merge with existing query params? Maybe not. Links with query params should
    //just go to register page.
    this.router.navigate(['/register'], {
      queryParams: {
[AuthenticationService.CALLBACK_ACTION_PARAM_NAME]:  AuthenticationService.REGISTER_ACTION}
    });
  }

  private pauseThenLogout() {
    //Log out the user if the login did not complete successfully.
    //Pause so user can see the error before logging out and being redirected to the landing page.
    timer(10000).subscribe(() => {
      //Log out the user if the registration did not complete successfully.
      this.authenticationService.logout();
    });
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

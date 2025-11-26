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

import {Component, HostBinding, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {LanguageService} from '../services/language.service';
import {LanguageLoader} from "../services/language.loader";
import {AuthenticationService} from "../services/authentication.service";
import {User} from "../model/user";
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {ChatService} from "../services/chat.service";
import {environment} from "../../environments/environment";
import {BrandingService} from "../services/branding.service";
import {BrandingInfo} from "../services/branding.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  //This CSS setting is used at the root of the whole app
  @HostBinding('class.rtl-wrapper') rtl: boolean = false;

  loading: boolean;
  isTBBPartner: boolean = false;

  constructor(private translate: TranslateService,
              private authenticationService: AuthenticationService,
              private chatService: ChatService,
              private router: Router,
              private languageLoader: LanguageLoader,
              private languageService: LanguageService,
              private brandingService: BrandingService,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.trackPageViews();

    this.authenticationService.loggedInUser$.subscribe(
      (user) => {
        this.onChangedLogin(user);
        this.checkBranding();
      }
    )

    //Register for language translation upload start and end events - which
    //drive the loading status.
    this.languageLoader.languageLoading$.subscribe(
      (loading: boolean) => {
        this.loading = loading;
      })

    //Register for language change events which are used to set the language and
    //appropriate Right to Left direction. That can only be set in this
    //component.
    this.languageService.languageChanged$.subscribe(
      () => {
        this.translate.use(this.languageService.getSelectedLanguage());
        this.rtl = this.languageService.isSelectedLanguageRtl();
      }
    );

    //Check for the partner query param and use it to configure the branding service
    this.route.queryParamMap.subscribe(
      (params) => {
        this.brandingService.setPartnerAbbreviation(params.get('p'));
        this.checkBranding();
      }
    );

    // Initial check for branding (handles case where query params are set before subscription)
    const initialParams = this.route.snapshot.queryParams;
    if (initialParams['p']) {
      this.brandingService.setPartnerAbbreviation(initialParams['p']);
    }
    this.checkBranding();

    // this language will be used as a fallback when a translation isn't
    // found in the current language. This forces loading of translations.
    this.translate.setDefaultLang('en');

    this.translate.use('en');
  }

  private onChangedLogin(user: User) {
    //If logged out
    if (user == null) {
      this.onLogout();
    }
  }

  private onLogout() {
    this.chatService.cleanUp();
    //Show login screen
    this.router.navigate(['login']);
  }

  private checkBranding(): void {
    // Use getBrandingInfoFromApi to get actual partner name for both registered and unregistered users
    this.brandingService.getBrandingInfoFromApi().subscribe(
      (response: BrandingInfo) => {
        this.isTBBPartner = response.partnerName === "Talent Beyond Boundaries";
      },
      (error) => {
        // On error, default to hiding chatbot
        this.isTBBPartner = false;
      }
    );
  }

  /**
   * Tracks page views in a Single Page Application (SPA) context using Google Analytics.
   *
   * In traditional websites, navigation between pages naturally triggers a page load,
   * which Google Analytics uses to track page views. However, in SPAs like those built with Angular,
   * navigation changes the content dynamically without reloading the entire page. This function
   * subscribes to Angular Router events to detect when navigation ends and a new "page" is viewed,
   * manually sending page view information to Google Analytics.
   *
   * The `NavigationEnd` event indicates a successful route change, at which point we use the
   * `gtag` function with the 'config' command to send the current page path to Google Analytics.
   *
   * Additionally, console logs are included for testing purposes.
   *
   * See, for example, https://blog.mestwin.net/add-google-analytics-to-angular-application-in-3-easy-steps
   */
  
  trackPageViews() {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        gtag('config', environment.googleAnalyticsId, {
          'page_path': event.urlAfterRedirects
        });
        // console.log('Sending Google Analytics tracking for: ', event.urlAfterRedirects);
        // console.log('Google Analytics property ID: ', environment.googleAnalyticsId);
      }
    });
  }
}

declare let gtag: Function;

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
import {ActivatedRoute, Router} from '@angular/router';
import {LanguageService} from '../../services/language.service';
import {initializePhraseAppEditor} from "ngx-translate-phraseapp";
import {BrandingInfo, BrandingService} from "../../services/branding.service";
import {AuthenticationService} from "../../services/authentication.service";

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})

export class LandingComponent implements OnInit {

  private brandingInfo: BrandingInfo;
  showUSAfghanInfo:boolean = false;

  constructor(private authenticationService: AuthenticationService,
              private brandingService: BrandingService,
              private router: Router,
              private route: ActivatedRoute,
              private languageService: LanguageService) { }

  ngOnInit() {

    //Note that we deliberately use a snapshot rather than a subscribe
    //so that we pick up the lang query even if there is a redirect.
    //eg like login redirects to /home if already authenticated.
    //With a subscribe, the query can be lost in the redirect.
    const lang = this.route.snapshot.queryParams['lang'];
    //Need to delay changing language otherwise you get ExpressionChangedAfterItHasBeenCheckedError
    setTimeout(
      () => this.languageService.changeLanguage(lang), 1000
    )

    //Branding info drives some aspects of this page
    this.brandingService.getBrandingInfo().subscribe(
      (response: BrandingInfo) => this.setBrandingInfo(response),
      (error) => console.log(error)
    );

    /**
     * Look for xlate query parameter which requests "in context" translation.
     * The value of the query parameter is the password which must be validated by the server
     * for "in context" translation to be enabled.
     */
    const xlate = this.route.snapshot.queryParams['xlate'];
    if (!xlate) {
      //No parameter (or no password supplied) - just continue normally
      this.proceed();
    } else {
      //Validate supplied password
      this.authenticationService.authenticateInContextTranslation(xlate).subscribe(
        () => {
          //Password validated, initialize "in context" translation
          LandingComponent.intializeInContextTranslation();
          //...and proceed
          this.proceed();
        },
        (error) => {
          //Password did not validate. Log the error quietly...
          console.log(error);
          //...and proceed as if no request was made
          this.proceed();
        }
      )
    }
  }

  private setBrandingInfo(brandingInfo:BrandingInfo) {
    this.brandingInfo = brandingInfo;
    this.showUSAfghanInfo = brandingInfo.websiteUrl?.includes("talentbeyondboundaries.org");
  }

  private proceed() {
    if (this.authenticationService.isAuthenticated()) {
      this.router.navigate(['/home']);
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

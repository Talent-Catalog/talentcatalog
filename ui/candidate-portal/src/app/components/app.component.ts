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

import {Component, HostBinding, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {LanguageService} from '../services/language.service';
import {LanguageLoader} from "../services/language.loader";
import {AuthenticationService} from "../services/authentication.service";
import {User} from "../model/user";
import {Router} from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  //This CSS setting is used at the root of the whole app
  @HostBinding('class.rtl-wrapper') rtl: boolean = false;

  loading: boolean;

  constructor(private translate: TranslateService,
              private authenticationService: AuthenticationService,
              private router: Router,
              private languageLoader: LanguageLoader,
              private languageService: LanguageService) {
  }

  ngOnInit(): void {

    this.authenticationService.loggedInUser$.subscribe(
      (user) => {
        this.onChangedLogin(user);
      }
    )

    //Register for language translation upload start and end events - which
    //drive the loading status.
    LanguageLoader.languageLoading$.subscribe(
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

    // this language will be used as a fallback when a translation isn't
    // found in the current language. This forces loading of translations.
    this.translate.setDefaultLang('en');

    this.translate.use('en');
  }

  private onChangedLogin(user: User) {
    //If logged out - show login screen
    if (user == null) {
      this.router.navigate(['login']);
    }
  }
}

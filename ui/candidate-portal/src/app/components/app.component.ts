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
import {initializePhraseAppEditor} from "ngx-translate-phraseapp";

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
              private languageLoader: LanguageLoader,
              private languageService: LanguageService) {

      //This is the Phrase "In context" translation configuration.
      //See https://phrase.com/blog/posts/angular-l10n-in-context-translation-editing/
      const config = {
        //This is the id associated with the TalentCatalog project - see Project Settings|API
        projectId: '7043871a7114505fdde77b5e2557331d',
        //Set this true to enable in context translation
        phraseEnabled: false,
        prefix: "{{__",
        suffix: "__}}",
        fullReparse: true
      };

      initializePhraseAppEditor(config);
  }

  ngOnInit(): void {

    //Register for language translation upload start and end events - which
    //drive the loading status.
    LanguageLoader.languageLoading$.subscribe(
      (loading: boolean) => {
        this.loading = loading;
      })

    //Register for language change events which are used to set the
    //appropriate Right to Left direction. That can only be set in this
    //component.
    this.languageService.languageChanged$.subscribe(
      () => this.rtl = this.languageService.isSelectedLanguageRtl()
    );

    // this language will be used as a fallback when a translation isn't
    // found in the current language. This forces loading of translations.
    this.translate.setDefaultLang('en');
  }
}

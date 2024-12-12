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

import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Observable, Subject} from 'rxjs';
import {TranslateLoader} from '@ngx-translate/core';

/**
 * This is an ngx-translate custom loader. We use it to fetch translations
 * from the server. Those translations are stored as JSON files up on Amazon AWS S3
 * in files.tbbtalent.org/translations.
 * <p/>
 * This custom loader is configured in app.module.ts - the import of TranslateModule there.
 * <p/>
 * See also https://github.com/ngx-translate/core#write--use-your-own-loader
 * <p/>
 * Note that the normal approach is to store the json translation files in the Angular code
 * base in the assets/i18n directory of the candidate portal code.
 * See https://github.com/ngx-translate/core#configuration.
 * <p/>
 * The advantage of our approach is that admin backend users can easily change translations
 * instantly from the Settings|Translations menu without making a new system release.
 */
@Injectable({
  providedIn: 'root'
})
export class LanguageLoader implements TranslateLoader {

  //Publish an Observable so that other objects can track the loading
  //process.
  //Had to make this static because I could not avoid two LanguageLoader
  //instances being created - JC.
  private static languageLoading = new Subject<boolean>();

  constructor(private http: HttpClient) { }

  //This implements the TranslateLoader interface.
  //It is called when the translations need to be loaded for a given lang.
  getTranslation(lang: string): Observable<any> {

    //Publish loading = true to the observable
    LanguageLoader.languageLoading.next(true);
    const loadLanguage$ = this.http.get(
      `${environment.apiUrl}/translate/translations/file/${lang}`
    );
    loadLanguage$.subscribe(
      //Publish loading = false to the observable - language has loaded.
      () => {LanguageLoader.languageLoading.next(false)}
    );

    return loadLanguage$;
  }

}

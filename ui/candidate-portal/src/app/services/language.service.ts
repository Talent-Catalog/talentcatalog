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

import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Language, SystemLanguage} from '../model/language';
import {Observable, Subject, throwError} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {Translation} from '../model/translation';
import {LocalStorageService} from 'angular-2-local-storage';

@Injectable({
  providedIn: 'root'
})
export class LanguageService {

  private apiUrl: string = environment.apiUrl + '/language';

  private languageChangedSource = new Subject<string>();
  languageChanged$ = this.languageChangedSource.asObservable();

  private languageSelectionDisabled: boolean = false;

  translations: Translation[];

  selectedLanguage: string;

  loading: boolean;

  usAfghan: boolean = false;

  constructor(private http: HttpClient,
              private localStorage: LocalStorageService) {
    this.languageSelectionDisabled = (this.localStorage.get('languageSelectionDisabled') as boolean);
    this.usAfghan = (this.localStorage.get('usAfghan') as boolean);
    this.selectedLanguage = (this.localStorage.get('language') as string) || 'en';
  }

  isLanguageSelectionDisabled(): boolean {
    return this.languageSelectionDisabled;
  }

  setLanguageSelectionDisabled(disabled: boolean) {
    this.localStorage.set('languageSelectionDisabled', disabled);
    this.languageSelectionDisabled = disabled;
  }

  isUsAfghan(): boolean {
    return this.usAfghan;
  }

  setUsAfghan(usAfghan: boolean) {
    this.localStorage.set('usAfghan', usAfghan);
    this.usAfghan = usAfghan;
  }

  listLanguages(): Observable<Language[]> {
    return this.http.get<Language[]>(`${this.apiUrl}`).pipe(
      map((items: Language[], index: number) => {
        return items.sort((a, b) => a.name.localeCompare(b.name, this.selectedLanguage));
      }),
      catchError(e => throwError(e))
    );
  }

  getLanguage(language){
    return this.http.get<Language>(`${this.apiUrl}/${language}`)
  }

  listSystemLanguages(): Observable<SystemLanguage[]> {
    return this.http.get<SystemLanguage[]>(`${this.apiUrl}/system`);
  }

  getSelectedLanguage(): string {
    return this.selectedLanguage;
  }

  isSelectedLanguageRtl(): boolean {
    //todo rtl languages need to be uploaded
    const rtl: boolean =  ['ar', 'fa', 'ps'].indexOf(this.selectedLanguage) >= 0;
    return rtl;
  }

  setSelectedLanguage(selectedLanguage: string) {
    this.selectedLanguage = selectedLanguage;
  }

  changeLanguage(lang) {
    if (lang) {
      this.localStorage.set('language', lang);
      this.setSelectedLanguage(lang);
    } else {
      lang = this.selectedLanguage;
    }
    this.languageChangedSource.next(lang);
  }
}

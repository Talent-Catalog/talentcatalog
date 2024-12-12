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
import {Language, SystemLanguage} from '../model/language';
import {Observable, Subject, throwError} from 'rxjs';
import {catchError, map, tap} from 'rxjs/operators';
import {LocalStorageService} from "./local-storage.service";

export class DataPickerNames {
  monthNames: string[];
  weekdayNames: string[];
}

@Injectable({
  providedIn: 'root'
})
export class LanguageService {

  private apiUrl: string = environment.apiUrl + '/language';

  private datePickerNames: DataPickerNames = null;

  private languageChangedSource = new Subject<string>();
  languageChanged$ = this.languageChangedSource.asObservable();

  private languageSelectionDisabled: boolean = false;

  private selectedLanguage: string;

  loading: boolean;

  usAfghan: boolean = false;

  constructor(private http: HttpClient,
              private localStorage: LocalStorageService) {
    this.languageSelectionDisabled = (this.localStorage.get('languageSelectionDisabled') as boolean);
    this.usAfghan = (this.localStorage.get('usAfghan') as boolean);
    this.setSelectedLanguage((this.localStorage.get('language') as string) || 'en');
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

  //todo This is only ever called with hardcoded 'english'. What is the point of it?
  //I think it is just used to default English as language in drop down
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

  changeLanguage(lang) {
    if (lang) {
      this.localStorage.set('language', lang);
      this.setSelectedLanguage(lang);
    } else {
      lang = this.selectedLanguage;
    }
    this.languageChangedSource.next(lang);
  }

  getDatePickerMonthName(month: number): string {
    let name: string = "";
    if (this.datePickerNames) {
      name = this.datePickerNames.monthNames[month - 1];
    }
    return name;
  }

  getDatePickerWeekdayName(weekday: number) {
    let name: string = "";
    if (this.datePickerNames) {
      name = this.datePickerNames.weekdayNames[weekday - 1];
    }
    return name;
  }

  /**
   * Callers just need to subscribe. They don't need to process the returned data. It is stored
   * locally - see the tap below.
   */
  public loadDatePickerLanguageData(): Observable<DataPickerNames> {
    return this.http.get<DataPickerNames>(
      `${this.apiUrl}/datepickernames/${this.selectedLanguage}`)
    .pipe(
      //Save the data locally
      tap(data => {this.datePickerNames = data})
    );
  }

  private setSelectedLanguage(selectedLanguage: string) {
    this.selectedLanguage = selectedLanguage;
  }
}

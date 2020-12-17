import {HostBinding, Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Language, SystemLanguage} from '../model/language';
import {Observable, of, throwError} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {Translation} from '../model/translation';
import {TranslateService} from '@ngx-translate/core';
import {LocalStorageService} from 'angular-2-local-storage';

@Injectable({
  providedIn: 'root'
})
export class LanguageService {

  private apiUrl: string = environment.apiUrl + '/language';

  translations: Translation[];

  selectedLanguage: string = 'en';

  @HostBinding('class.rtl-wrapper') rtl: boolean;

  loading: boolean;

  constructor(private http: HttpClient,
              private translate: TranslateService,
              private localStorage: LocalStorageService) { }

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

  setSelectedLanguage(selectedLanguage: string) {
    this.selectedLanguage = selectedLanguage;
  }

  loadTranslations(): Observable<boolean> {
    if (this.selectedLanguage != 'en'){
      return this.http.get<Translation[]>(`${this.apiUrl}/translations`).pipe(map(result => {
        this.translations = result;
        return true;
      }));
    } else {
      this.translations = [];
      return of(true);
    }
  }

  getTranslation(object, type){
    if (this.translations){
      let translation =  this.translations.find(t => t.objectType == type && t.objectId == object.id);
      return translation ? translation.value : object.name;
    }
    return object.name;
  }

  setLanguage(lang) {
    // Add .rtl-wrapper class to app root if the language is arabic
    this.loading = true;
    this.rtl = lang === 'ar';
    this.localStorage.set('language', lang);
    this.setSelectedLanguage(lang);
    this.translate.use(lang);
    this.loadTranslations().subscribe(
      result => {
        this.loading = false;
      }, error => {
        this.loading = false;
      });
  }

}

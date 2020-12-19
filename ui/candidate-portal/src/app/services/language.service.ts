import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Language, SystemLanguage} from '../model/language';
import {Observable, Subject, throwError} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {Translation} from '../model/translation';
import {TranslateService} from '@ngx-translate/core';
import {LocalStorageService} from 'angular-2-local-storage';


//todo Instead of this I can make LanguageService a CustomLoader
export function createTranslateLoader(http: HttpClient) {
  return {
    //todo document this
    getTranslation(lang: string): Observable<any> {
      return http.get(`${environment.apiUrl}/language/translations/file/${lang}`);
    }
  };
}

@Injectable({
  providedIn: 'root'
})
export class LanguageService {

  private apiUrl: string = environment.apiUrl + '/language';

  private languageChangedSource = new Subject<string>();
  languageChanged$ = this.languageChangedSource.asObservable();

  translations: Translation[];

  selectedLanguage: string;

  loading: boolean;

  constructor(private http: HttpClient,
              private translate: TranslateService,
              private localStorage: LocalStorageService) {

    this.selectedLanguage = (this.localStorage.get('language') as string) || 'en';
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
    return this.selectedLanguage === 'ar';
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
    this.translate.use(lang);
    this.languageChangedSource.next(lang);
  }

}

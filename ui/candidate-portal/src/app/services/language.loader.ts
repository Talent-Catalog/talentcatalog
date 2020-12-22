import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Observable, Subject} from 'rxjs';
import {TranslateLoader} from '@ngx-translate/core';

/**
 * This is an ngx-translate custom loader. We use it to fetch translations
 * from the server (which are actually stored up on Amazon AWS).
 * <p/>
 * This is configured in app.module.ts - the import of TranslateModule there.
 * <p/>
 * See also https://github.com/ngx-translate/core#write--use-your-own-loader
 */
@Injectable({
  providedIn: 'root'
})
export class LanguageLoader implements TranslateLoader {

  //Only needed to confirm that two LanguageLoader instances are created.
  //Might need to report this as a bug to ngx-translate
  private static count: number = 0;

  //Publish an Observable so that other objects can track the loading
  //process.
  //Had to make this static because I could not avoid two LanguageLoader
  //instances being created - JC.
  private static languageLoading = new Subject<boolean>();
  static languageLoading$ = LanguageLoader.languageLoading.asObservable();

  //Used to demonstrate two instances.
  instance: string;

  constructor(private http: HttpClient) {
    //Bit of debugging which hopefully will help up eventually nail down why
    //two instances of this class are created instead of just one.
    LanguageLoader.count++;
    this.instance = "Loader " + LanguageLoader.count;
    console.log("Creating Language Loader " + this.instance);
  }

  //This implements the the TranslateLoader interface.
  //It is called when the translations need to be loaded for a given lang.
  getTranslation(lang: string): Observable<any> {

    //Publish loading = true to the observable
    LanguageLoader.languageLoading.next(true);
    const loadLanguage$ = this.http.get(`${environment.apiUrl}/language/translations/file/${lang}`);
    loadLanguage$.subscribe(
      //Publish loading = false to the observable - language has loaded.
      () => {LanguageLoader.languageLoading.next(false)}
    );

    return loadLanguage$;
  }

}

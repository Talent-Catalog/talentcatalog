import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';
import {LanguageService} from "./language.service";

@Injectable()
export class LanguageInterceptor implements HttpInterceptor {

  constructor(private languageService: LanguageService) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // add selected language header
    let selectedLanguage = this.languageService.getSelectedLanguage();
    console.log(selectedLanguage);
    if (selectedLanguage) {
      request = request.clone({
        setHeaders: {
          'X-Language': selectedLanguage
        }
      });
    }

    return next.handle(request);
  }
}

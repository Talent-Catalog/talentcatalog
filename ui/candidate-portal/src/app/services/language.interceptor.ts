import {Injectable} from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import {Observable} from 'rxjs';
import {LanguageService} from "./language.service";

/**
 * This inserts the user's currently selected language into the X-Language
 * header of any Http requests.
 * <p/>
 * See LanguageFilter.java on the Server side which updates the user's
 * selected language according to the X-Language header value.
 */
@Injectable()
export class LanguageInterceptor implements HttpInterceptor {

  constructor(private languageService: LanguageService) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // add selected language header
    const selectedLanguage = this.languageService.getSelectedLanguage();
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

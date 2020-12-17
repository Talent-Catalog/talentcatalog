import {Component, HostBinding} from '@angular/core';
import {Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {LocalStorageService} from 'angular-2-local-storage';
import {LanguageService} from '../services/language.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  @HostBinding('class.rtl-wrapper') rtl: boolean;
  loading: boolean;

  constructor(private router: Router,
              private translate: TranslateService,
              private languageService: LanguageService,
              private localStorage: LocalStorageService) {

    // this language will be used as a fallback when a translation isn't found in the current language
    translate.setDefaultLang('en');
    const lang = (this.localStorage.get('language') as string) || 'en';
    // Add .rtl-wrapper class to app root if the language is arabic
    this.rtl = lang === 'ar';
    this.languageService.setLanguage(lang);
  }

  setLanguage(lang) {
    // Add .rtl-wrapper class to app root if the language is arabic
    this.rtl = lang === 'ar';
    this.languageService.setLanguage(lang);
  }

}

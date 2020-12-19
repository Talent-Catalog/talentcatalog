import {Component, HostBinding} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {LanguageService} from '../services/language.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  @HostBinding('class.rtl-wrapper') rtl: boolean = false;

  //todo Never set?
  loading: boolean;

  constructor(private translate: TranslateService,
              private languageService: LanguageService) {

    // this language will be used as a fallback when a translation isn't
    // found in the current language. This forces loading of translations
    //
    this.translate.setDefaultLang('en');

    //todo Could listen to translate onLangChange
    this.languageService.languageChanged$.subscribe(
      () => this.rtl = this.languageService.isSelectedLanguageRtl()
    );
  }
}

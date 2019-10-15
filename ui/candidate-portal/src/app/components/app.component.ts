import {Component} from '@angular/core';
import {NavigationStart, Router} from "@angular/router";
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  showHeader: boolean = true;

  constructor(private router: Router,
              private translate: TranslateService) {

    // this language will be used as a fallback when a translation isn't found in the current language
    translate.setDefaultLang('en');

    // the lang to use, if the lang isn't available, it will use the current loader to get them
    translate.use('en');

    router.events.forEach((event) => {
      if (event instanceof NavigationStart) {
        if (event['url'] == '/login' || event['url'].indexOf('/reset-password') != -1) {
          this.showHeader = false;
        } else {
          this.showHeader = true;
        }
      }
    });
  }
}

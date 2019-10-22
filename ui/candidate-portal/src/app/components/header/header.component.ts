import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {Router} from "@angular/router";
import {TranslateService} from '@ngx-translate/core';
import {SystemLanguage} from "../../model/language";
import {LanguageService} from "../../services/language.service";
import {User} from "../../model/user";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  @Input() hideHeader: boolean;

  @Output() languageUpdated = new EventEmitter();

  isNavbarCollapsed=true;

  languages: SystemLanguage[];
  error: any;
  user: User;

  constructor(private authService: AuthService,
              private router: Router,
              private translate: TranslateService,
              public languageService: LanguageService) { }

  ngOnInit() {
    this.user = this.authService.getLoggedInUser();
    this.languageService.listSystemLanguages().subscribe(
      (response) => {
        this.languages = response;
      },
      (error) => {
        this.error = error;
      }
    );
  }

  logout() {
    this.authService.logout();
    this.user = null;
    this.router.navigate(['']);
  }

  useLanguage(language: string) {
    this.translate.use(language);
    this.languageService.setSelectedLanguage(language);
    this.languageUpdated.emit(language);
  }

  get selectedLanguage() {
    let language = null;
    if (this.languages) {
      language = this.languages.find(lang => lang.language === this.languageService.getSelectedLanguage());
    }
    return language ? language.label : 'Language';
  }
}

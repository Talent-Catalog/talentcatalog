import {Component, Input, OnInit} from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {Router} from "@angular/router";
import {TranslateService} from '@ngx-translate/core';
import {SystemLanguage} from "../../model/language";
import {LanguageService} from "../../services/language.service";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  @Input() hideHeader: boolean;
  isNavbarCollapsed=true;
  
  languages: SystemLanguage[];
  error: any;

  constructor(private authService: AuthService,
              private router: Router,
              private translate: TranslateService,
              private languageService: LanguageService) { }

  ngOnInit() {
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
    this.router.navigate(['']);
  }

  useLanguage(language: string) {
    this.translate.use(language);
  }
}

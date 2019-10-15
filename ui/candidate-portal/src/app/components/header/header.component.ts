import {Component, Input, OnInit} from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {Router} from "@angular/router";
import {TranslateService} from '@ngx-translate/core';
import {SystemLanguage} from "../../model/language";
import {LanguageService} from "../../services/language.service";
import {Candidate} from "../../model/candidate";

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
  candidate: Candidate;

  constructor(private authService: AuthService,
              private router: Router,
              private translate: TranslateService,
              public languageService: LanguageService) { }

  ngOnInit() {
    this.candidate = this.authService.getLoggedInCandidate()
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
    this.languageService.setSelectedLanguage(language);
  }
}

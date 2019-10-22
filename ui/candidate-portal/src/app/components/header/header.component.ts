import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {Router} from "@angular/router";
import {SystemLanguage} from "../../model/language";
import {LanguageService} from "../../services/language.service";

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

  constructor(public authService: AuthService,
              private router: Router,
              public languageService: LanguageService) { }

  ngOnInit() {
    this.languageService.listSystemLanguages().subscribe(
      (response) => this.languages = response,
      (error) => this.error = error
    );
  }

  logout() {
    this.authService.logout().subscribe(() => this.router.navigate(['']));
  }

  setLanguage(language: string) {
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

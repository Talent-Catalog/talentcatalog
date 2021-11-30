/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, Input, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {Router} from '@angular/router';
import {SystemLanguage} from '../../model/language';
import {LanguageService} from '../../services/language.service';
import {CandidateService} from "../../services/candidate.service";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  @Input() hideHeader: boolean;

  isNavbarCollapsed = true;

  languages: SystemLanguage[];
  error: any;

  constructor(public authService: AuthService,
              public candidateService: CandidateService,
              private router: Router,
              public languageService: LanguageService) { }

  ngOnInit() {
    this.languageService.listSystemLanguages().subscribe(
      (response) => this.languages = response,
      (error) => this.error = error
    );
  }

  logout() {
    this.authService.logout().subscribe(
      () => {
        this.isNavbarCollapsed = true;
        this.router.navigate(['']);
      }
    );
  }

  setLanguage(language: string) {
    this.isNavbarCollapsed = true;
    this.languageService.changeLanguage(language);
  }

  get selectedLanguage() {
    let language = null;
    if (this.languages) {
      language = this.languages.find(lang => lang.language !== this.languageService.getSelectedLanguage());
    }
    return language ? language.label : 'Language';
  }
}

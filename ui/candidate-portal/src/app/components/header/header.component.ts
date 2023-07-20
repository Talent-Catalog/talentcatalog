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
import {ActivatedRoute, Router} from '@angular/router';
import {SystemLanguage} from '../../model/language';
import {LanguageService} from '../../services/language.service';
import {CandidateService} from "../../services/candidate.service";
import {BrandingInfo, BrandingService} from "../../services/branding.service";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  @Input() hideHeader: boolean;

  isNavbarCollapsed = true;

  languages: SystemLanguage[];
  logo: string;
  websiteUrl: string;
  error: any;

  constructor(public authService: AuthService,
              private brandingService: BrandingService,
              public candidateService: CandidateService,
              private router: Router,
              private route: ActivatedRoute,
              public languageService: LanguageService) { }

  ngOnInit() {
    this.languageService.listSystemLanguages().subscribe(
      (response) => this.languages = response,
      (error) => this.error = error
    );

    //Check for the partner query param and use it to configure the branding service
    this.route.queryParamMap.subscribe(
      (params) => {
        this.brandingService.setPartnerAbbreviation(params.get('p'));
        this.showBranding();
      }
    );
  }

  private showBranding(): void {
    this.brandingService.getBrandingInfo().subscribe(
      (response: BrandingInfo) => {
        this.logo = response.logo;
        this.websiteUrl = response.websiteUrl;
      },
      (error) => this.error = error
    );
  }

  logout() {
    this.authService.logout().subscribe(
      () => {
        // Clear candidate number in local storage (used to display in header)
        this.candidateService.clearCandNumberStorage();
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
      language = this.languages.find(lang => lang.language === this.languageService.getSelectedLanguage());
    }
    return language ? language.label : 'Language';
  }

  isStagingEnv(): boolean {
    return window.location.host == 'tctalent-test.org';
  }

  isLocalEnv(): boolean {
    return window.location.host == 'localhost:4200';
  }
}

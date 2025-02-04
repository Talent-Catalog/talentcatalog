/*
 * Copyright (c) 2024 Talent Catalog.
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

import {Component, OnInit} from '@angular/core';
import {CandidateService} from "../../services/candidate.service";
import {Candidate, CandidateStatus} from "../../model/candidate";
import {User} from "../../model/user";
import {LanguageService} from "../../services/language.service";
import {US_AFGHAN_SURVEY_TYPE} from "../../model/survey-type";
import {BrandingService} from "../../services/branding.service";
import {ExternalLinkService} from "../../services/external-link.service";
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],

})
export class HomeComponent implements OnInit {

  loading: boolean;
  error: any;

  candidate: Candidate;
  user: User;
  lang: string;
  partnerName: string;
  emailVerified: boolean;

  constructor(private candidateService: CandidateService,
              private languageService: LanguageService,
              private brandingService: BrandingService,
              private externalLinkService: ExternalLinkService,
              private userService: UserService
            ) {
  }

  openModal(modal: any) {
    modal.openModal(); // Call the modal's open method
  }

  ngOnInit() {
    this.loading = true;
    this.lang = this.languageService.getSelectedLanguage();

    this.userService.getMyUser().subscribe(
      (user) => {
        this.emailVerified = user.emailVerified;
      },
      (error) => {
        this.error = error;
      }
    );

    this.candidateService.getStatus().subscribe(
      (candidate) => {
        this.candidate = candidate || ({status: CandidateStatus.draft} as Candidate);
        this.user = this.candidate.user;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    //The purpose of this call is just to check whether this is a US Afghan candidate,
    //and, if so, to turn off the language selection.
    this.candidateService.getCandidateSurvey().subscribe(
      (response) => {
        const usAfghan: boolean = response?.surveyType?.id === US_AFGHAN_SURVEY_TYPE
        this.languageService.setUsAfghan(usAfghan);
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    this.brandingService.getBrandingInfo().subscribe((brandingInfo) => this.partnerName = brandingInfo.partnerName)

  }

  /**
   * Return candidate's first name and last name, or empty string if unknown.
   */
  getCandidateName(): string {
    let name = '';
    if (this.user) {
      if (this.user.firstName) {
        name += this.user.firstName.trim();
      }
      if (this.user.lastName) {
        name += ' ' + this.user.lastName.trim();
      }
    }
    return name;
  }

  getPartnerName(): string {
    return this.partnerName;
  }

  getEligibilityLink(): string {
    return this.externalLinkService.getLink('eligibility', this.lang);
  }
}


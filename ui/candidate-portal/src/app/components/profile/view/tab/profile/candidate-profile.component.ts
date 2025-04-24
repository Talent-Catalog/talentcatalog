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

import {Component, Input, OnInit} from '@angular/core';
import {CandidateService} from "../../../../../services/candidate.service";
import {Candidate} from "../../../../../model/candidate";
import {ActivatedRoute} from "@angular/router";
import {SurveyType} from "../../../../../model/survey-type";
import {LangChangeEvent, TranslateService} from "@ngx-translate/core";
import {OccupationService} from "../../../../../services/occupation.service";
import {Occupation} from "../../../../../model/occupation";
import {CountryService} from "../../../../../services/country.service";
import {Country} from "../../../../../model/country";
import {EducationMajorService} from "../../../../../services/education-major.service";
import {EducationMajor} from "../../../../../model/education-major";
import {LanguageService} from "../../../../../services/language.service";
import {Language} from "../../../../../model/language";
import {LanguageLevelService} from "../../../../../services/language-level.service";
import {LanguageLevel} from "../../../../../model/language-level";
import {SurveyTypeService} from "../../../../../services/survey-type.service";
import {isOppStageGreaterThanOrEqualTo} from "../../../../../model/candidate-opportunity";
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ChangePasswordComponent} from '../../../../account/change-password/change-password.component';

@Component({
  selector: 'app-candidate-profile',
  templateUrl: './candidate-profile.component.html',
  styleUrls: ['./candidate-profile.component.scss']
})
export class CandidateProfileComponent implements OnInit {

  error: any;
  loading: boolean;
  subscription;

  _loading = {
    occupations: true,
    countries: true,
    majors: true,
    languages: true,
    languageLevels: true,
    surveyTypes: true,
  };

  occupations: Occupation[];
  countries: Country[];
  majors: EducationMajor[];
  languages: Language[];
  languageLevels: LanguageLevel[];
  surveyTypes: SurveyType[];
  @Input() candidate: Candidate;
  usAfghan: boolean;

  constructor(private candidateService: CandidateService,
              public translateService: TranslateService,
              private occupationService: OccupationService,
              private countryService: CountryService,
              private educationMajorService: EducationMajorService,
              private languageService: LanguageService,
              private languageLevelService: LanguageLevelService,
              private surveyTypeService: SurveyTypeService,
              private route: ActivatedRoute,
              private modalService: NgbModal) { }

  ngOnInit() {
    const lang = this.route.snapshot.queryParams['lang'];
    //Need to delay changing language otherwise you get ExpressionChangedAfterItHasBeenCheckedError
    setTimeout(
      () => this.languageService.changeLanguage(lang), 1000
    )
    // It is important to have this line because otherwise data is missing from the DOM on tab change (Only appears with refresh). Issue #2129
    this.loadDropDownData();
    // listen for change of language and save
    this.subscription = this.translateService.onLangChange.subscribe((event: LangChangeEvent) => {
      this.loadDropDownData();
    });

    // this.candidateService.getProfile().subscribe(
    //   (response) => {
    //     this.candidate = response;
    //     this.usAfghan = response.surveyType?.id === US_AFGHAN_SURVEY_TYPE;
    //     this.loading = false;
    //   },
    //   (error) => {
    //     this.error = error;
    //     this.loading = false;
    //   });


  }

  loadDropDownData() {
    this._loading.occupations = true;
    this._loading.countries = true;
    this._loading.majors = true;
    this._loading.languages = true;
    this._loading.languageLevels = true;
    this._loading.surveyTypes = true;

    this.occupationService.listOccupations().subscribe(
      (response) => {
        this.occupations = response;
        this._loading.occupations = false;
      },
      (error) => {
        this.error = error;
        this._loading.occupations = false;
      }
    );

    this.countryService.listCountries().subscribe(
      (response) => {
        this.countries = response;
        this._loading.countries = false;
      },
      (error) => {
        this.error = error;
        this._loading.countries = false;
      }
    );

    this.educationMajorService.listMajors().subscribe(
      (response) => {
        this.majors = response;
        this._loading.majors = false;
      },
      (error) => {
        this.error = error;
        this._loading.majors = false;
      }
    );

    this.languageService.listLanguages().subscribe(
      (response) => {
        this.languages = response;
        this._loading.languages = false;
      },
      (error) => {
        this.error = error;
        this._loading.languages = false;
      }
    );

    this.languageLevelService.listLanguageLevels().subscribe(
      (response) => {
        this.languageLevels = response;
        this._loading.languageLevels = false;
      },
      (error) => {
        this.error = error;
        this._loading.languageLevels = false;
      }
    );

    this.surveyTypeService.listActiveSurveyTypes().subscribe(
      (response) => {
        this.surveyTypes = response;
        this._loading.surveyTypes = false;
      },
      (error) => {
        this.error = error;
        this._loading.surveyTypes = false;
      }
    );
  }

  getSurveyTypeName() {
    return this.surveyTypes?.find(s => s.id === this.candidate?.surveyType?.id)?.name;
  }

  getCountryName(country: Country) {
    return this.countries?.find(c => c.id === country?.id)?.name;
  }

  showRelocatedAddress(): boolean {
    let showRelocated = false;
    // If candidate has any relocated address fields
    // Or if candidate has candidate opportunities of which some are past the job offer stage
    // THEN show relocated address
    if (this.candidate?.relocatedAddress || this.candidate?.relocatedCity
      || this.candidate?.relocatedState || this.candidate?.relocatedCountry) {
      showRelocated = true;
    } else if (this.candidate?.candidateOpportunities.length > 0) {
      showRelocated = this.candidate.candidateOpportunities.some(co => {
        return isOppStageGreaterThanOrEqualTo(co?.lastActiveStage, "acceptance");
      })
    }
    return showRelocated;
  }

  openChangePasswordModal() {
    const  changePasswordModal = this.modalService.open(ChangePasswordComponent, {
      centered: true
    });
  }
}

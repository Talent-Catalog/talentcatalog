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
import {IntakeComponentTabBase} from '../../../../../util/intake/IntakeComponentTabBase';
import {
  Candidate,
  CandidateIntakeData,
  CandidateVisa,
  getIeltsScoreTypeString
} from '../../../../../../model/candidate';
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../../../services/candidate.service";
import {CountryService} from "../../../../../../services/country.service";
import {EducationLevelService} from "../../../../../../services/education-level.service";
import {OccupationService} from "../../../../../../services/occupation.service";
import {LanguageLevelService} from "../../../../../../services/language-level.service";
import {CandidateNoteService} from "../../../../../../services/candidate-note.service";
import {AuthService} from "../../../../../../services/auth.service";
import {CandidateVisaJobService} from "../../../../../../services/candidate-visa-job.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Country} from "../../../../../../model/country";
import {LocalStorageService} from "angular-2-local-storage";

@Component({
  selector: 'app-visa-check-au',
  templateUrl: './visa-check-au.component.html',
  styleUrls: ['./visa-check-au.component.scss']
})
export class VisaCheckAuComponent extends IntakeComponentTabBase implements OnInit {

  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  visaRecord: CandidateVisa;
  loading: boolean;
  @Input() nationalities: Country[];
  saving: boolean;
  selectedJobIndex: number;
  currentYear: string;
  birthYear: string;

  constructor(candidateService: CandidateService,
              countryService: CountryService,
              educationLevelService: EducationLevelService,
              occupationService: OccupationService,
              languageLevelService: LanguageLevelService,
              noteService: CandidateNoteService,
              authService: AuthService,
              private candidateVisaJobService: CandidateVisaJobService,
              private modalService: NgbModal,
              private fb: FormBuilder,
              private localStorageService: LocalStorageService) {
    super(candidateService, countryService, educationLevelService, occupationService, languageLevelService, noteService, authService)
  }

  onDataLoaded(init: boolean) {
    if (init) {
      this.visaRecord = this.candidateIntakeData?.candidateVisaChecks?.find(v => v.country.id == 6191);
      this.setSelectedVisaCheckIndex(this.candidateIntakeData?.candidateVisaChecks?.indexOf(this.visaRecord));
      this.currentYear = new Date().getFullYear().toString();
      this.birthYear = this.candidate?.dob?.toString().slice(0, 4);
    }
  }

  get selectedCountry(): string {
    return this.visaRecord?.country?.name;
  }

  get ieltsScoreType(): string {
    return getIeltsScoreTypeString(this.candidate);
  }

  private setSelectedVisaCheckIndex(index: number) {
    this.localStorageService.set('VisaCheckIndex', index);
  }

  updateJobIndex(index: number){
    this.selectedJobIndex = index;
  }
}

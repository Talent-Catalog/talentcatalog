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

import {Component, Input} from '@angular/core';
import {IntakeComponentTabBase} from '../../../../../util/intake/IntakeComponentTabBase';
import {CandidateIntakeData, CandidateVisa} from '../../../../../../model/candidate';
import {FormGroup} from '@angular/forms';
import {CandidateService} from "../../../../../../services/candidate.service";
import {CountryService} from "../../../../../../services/country.service";
import {EducationLevelService} from "../../../../../../services/education-level.service";
import {OccupationService} from "../../../../../../services/occupation.service";
import {LanguageLevelService} from "../../../../../../services/language-level.service";
import {CandidateNoteService} from "../../../../../../services/candidate-note.service";
import {AuthService} from "../../../../../../services/auth.service";
import {LocalStorageService} from "angular-2-local-storage";

@Component({
  selector: 'app-visa-check-ca',
  templateUrl: './visa-check-ca.component.html',
  styleUrls: ['./visa-check-ca.component.scss']
})
export class VisaCheckCaComponent extends IntakeComponentTabBase {
  @Input() selectedIndex: number;
  @Input() candidateIntakeData: CandidateIntakeData;
  visaRecord: CandidateVisa;
  form: FormGroup;

  public tbbEligibilityHide = true;
  public caEligibilityHide = true;
  public healthAssessHide = true;
  public characterAssessHide = true;
  public securityAssessHide = true;

  constructor(candidateService: CandidateService,
              countryService: CountryService,
              educationLevelService: EducationLevelService,
              occupationService: OccupationService,
              languageLevelService: LanguageLevelService,
              noteService: CandidateNoteService,
              authService: AuthService,
              private localStorageService: LocalStorageService) {
    super(candidateService, countryService, educationLevelService, occupationService, languageLevelService, noteService, authService)
  }

  onDataLoaded(init: boolean) {
    if (init) {
      this.visaRecord = this.candidateIntakeData?.candidateVisaChecks?.find(v => v.country.id == 6216);
      this.setSelectedVisaCheckIndex(this.candidateIntakeData?.candidateVisaChecks?.indexOf(this.visaRecord));
    }
  }

  private get myRecord(): CandidateVisa {
    return this.candidateIntakeData.candidateVisaChecks ?
      this.candidateIntakeData.candidateVisaChecks[this.selectedIndex]
      : null;
  }

  private setSelectedVisaCheckIndex(index: number) {
    this.localStorageService.set('VisaCheckIndex', index);
  }
}

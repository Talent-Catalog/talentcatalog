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
import {EnumOption, enumOptions} from '../../../../util/enum';
import {Candidate, IeltsScore, IeltsStatus, MaritalStatus, YesNo, YesNoUnsure} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {EducationLevel} from '../../../../model/education-level';
import {Occupation} from '../../../../model/occupation';
import {LanguageLevel} from '../../../../model/language-level';
import {Nationality} from '../../../../model/nationality';
import {generateYearArray} from '../../../../util/year-helper';

@Component({
  selector: 'app-marital-status',
  templateUrl: './marital-status.component.html',
  styleUrls: ['./marital-status.component.scss']
})
export class MaritalStatusComponent extends IntakeComponentBase implements OnInit {

  @Input() educationLevels: EducationLevel[];
  @Input() occupations: Occupation[];
  @Input() languageLevels: LanguageLevel[];
  @Input() nationalities: Nationality[];

  public maritalStatusOptions: EnumOption[] = enumOptions(MaritalStatus);
  public partnerRegisteredOptions: EnumOption[] = enumOptions(YesNoUnsure);
  public partnerEnglishOptions: EnumOption[] = enumOptions(YesNo);
  public partnerIeltsOptions: EnumOption[] = enumOptions(IeltsStatus);
  public partnerIeltsScoreOptions: EnumOption[] = enumOptions(IeltsScore);
  years: number[];

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      maritalStatus: [this.candidateIntakeData?.maritalStatus],
      partnerRegistered: [this.candidateIntakeData?.partnerRegistered],
      partnerCandId: [this.candidateIntakeData?.partnerCandidate?.id],
      partnerEduLevelId: [this.candidateIntakeData?.partnerEduLevel?.id],
      partnerEduLevelNotes: [this.candidateIntakeData?.partnerEduLevelNotes],
      partnerOccupationId: [this.candidateIntakeData?.partnerOccupation?.id],
      partnerEnglish: [this.candidateIntakeData?.partnerEnglish],
      partnerEnglishLevelId: [this.candidateIntakeData?.partnerEnglishLevel?.id],
      partnerIelts: [this.candidateIntakeData?.partnerIelts],
      partnerIeltsScore: [this.candidateIntakeData?.partnerIeltsScore],
      partnerIeltsYr: [this.candidateIntakeData?.partnerIeltsYr],
      partnerCitizenshipId: [this.candidateIntakeData?.partnerCitizenship?.id],
    });
    this.years = generateYearArray(1950, true);
  }

  get maritalStatus() {
    return this.form.value?.maritalStatus;
  }

  get partnerRegistered() {
    return this.form.value?.partnerRegistered;
  }

  get partnerEnglish() {
    return this.form.value?.partnerEnglish;
  }

  get partnerIelts() {
    return this.form.value?.partnerIelts;
  }

  get partnerCandidate(): Candidate {
    return this.candidateIntakeData.partnerCandidate ?
      this.candidateIntakeData.partnerCandidate : null;
  }

  get hasPartner(): boolean {
    let found: boolean = false;
    if (this.maritalStatus) {
      if (this.maritalStatus === 'Engaged') {
        found = true;
      } else if (this.maritalStatus === 'Married') {
        found = true;
      } else if (this.maritalStatus === 'Defacto'){
        found = true;
      }
    }
    return found;
  }

  updatePartnerCand($event) {
    this.form.controls['partnerCandId'].patchValue($event.id);
    if (this.partnerCandidate) {
      this.partnerCandidate.user.firstName = $event.user.firstName;
      this.partnerCandidate.user.lastName = $event.user.lastName;
      this.partnerCandidate.candidateNumber = $event.candidateNumber;
    }
  }

  get takenIelts(): boolean {
    let ielts: boolean = false;
    if (this.form?.value) {
      if (this.form.value.partnerIelts === 'YesGeneral' || this.form.value.partnerIelts === 'YesAcademic') {
        ielts = true;
      }
    }
    return ielts;
  }

  get eduLevelSelected(): boolean {
    return this.form?.value?.partnerEduLevelId != null;
  }


}

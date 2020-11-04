import {Component, Input, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {MaritalStatus, YesNo, YesNoUnsure} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {EducationLevel} from '../../../../model/education-level';
import {Occupation} from '../../../../model/occupation';
import {LanguageLevel} from '../../../../model/language-level';
import {Nationality} from '../../../../model/nationality';

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
  public partnerIELTSOptions: EnumOption[] = enumOptions(YesNoUnsure);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      maritalStatus: [this.candidateIntakeData?.maritalStatus],
      partnerRegistered: [this.candidateIntakeData?.partnerRegistered],
      partnerCandNumber: [this.candidateIntakeData?.partnerCandNumber],
      partnerEduLevel: [this.candidateIntakeData?.partnerEduLevel],
      partnerProfession: [this.candidateIntakeData?.partnerProfession],
      partnerEnglish: [this.candidateIntakeData?.partnerEnglish],
      partnerEnglishLevel: [this.candidateIntakeData?.partnerEnglishLevel],
      partnerIELTS: [this.candidateIntakeData?.partnerIELTS],
      partnerCitizenship: [this.candidateIntakeData?.partnerCitizenship],
    });
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

  get partnerCandNumber() {
    return '15000'
    //return this.form.value?.partnerCandNumber;
  }

  get isMarriedEngaged(): boolean {
    let found: boolean = false;
    if (this.maritalStatus) {
      if (this.maritalStatus === 'Engaged') {
        found = true;
      } else if (this.maritalStatus === 'Married') {
        found = true;
      }
    }
    return found;
  }

  get partnerSpeakEnglish(): boolean {
    let found: boolean = false;
    if (this.partnerEnglish) {
      if (this.partnerEnglish === 'Engaged') {
        found = true;
      } else if (this.maritalStatus === 'Married') {
        found = true;
      }
    }
    return found;
  }

  updatePartnerCandNumber($event) {
    this.form.value.partnerCandNumber = $event;
  }
}

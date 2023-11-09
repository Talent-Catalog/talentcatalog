import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {VisaCheckComponentBase} from "../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-character-assessment',
  templateUrl: './character-assessment.component.html',
  styleUrls: ['./character-assessment.component.scss']
})
export class CharacterAssessmentComponent extends VisaCheckComponentBase implements OnInit {

//Drop down values for enumeration
  characterAssessmentOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheck?.id],
      visaCountryId: [this.visaCheck?.country?.id],
      visaCharacterAssessment: [this.visaCheck?.characterAssessment],
      visaCharacterAssessmentNotes: [this.visaCheck?.characterAssessmentNotes],
    });
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.visaCharacterAssessment) {
      if (this.form.value.visaCharacterAssessment === 'Yes') {
        found = true
      }
      if (this.form.value.visaCharacterAssessment === 'No') {
        found = true
      }
    }
    return found;
  }

}

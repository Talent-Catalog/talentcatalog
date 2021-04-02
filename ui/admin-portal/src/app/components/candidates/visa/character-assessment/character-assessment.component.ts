import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-character-assessment',
  templateUrl: './character-assessment.component.html',
  styleUrls: ['./character-assessment.component.scss']
})
export class CharacterAssessmentComponent extends IntakeComponentBase implements OnInit {

//Drop down values for enumeration
  characterAssessmentOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheckRecord?.id],
      visaCountryId: [this.visaCheckRecord?.country?.id],
      visaCharacterAssessment: [this.visaCheckRecord?.characterAssessment],
      visaCharacterAssessmentNotes: [this.visaCheckRecord?.characterAssessmentNotes],
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

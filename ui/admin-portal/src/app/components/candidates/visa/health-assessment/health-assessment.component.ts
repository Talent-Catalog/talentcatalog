import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {VisaCheckComponentBase} from "../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-health-assessment',
  templateUrl: './health-assessment.component.html',
  styleUrls: ['./health-assessment.component.scss']
})
export class HealthAssessmentComponent extends VisaCheckComponentBase implements OnInit {

//Drop down values for enumeration
  healthAssessmentOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheck?.id],
      visaCountryId: [this.visaCheck?.country?.id],
      visaHealthAssessment: [this.visaCheck?.healthAssessment],
      visaHealthAssessmentNotes: [this.visaCheck?.healthAssessmentNotes],
    });
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.visaHealthAssessment) {
      if (this.form.value.visaHealthAssessment === 'Yes') {
        found = true
      }
      if (this.form.value.visaHealthAssessment === 'No') {
        found = true
      }
    }
    return found;
  }

}

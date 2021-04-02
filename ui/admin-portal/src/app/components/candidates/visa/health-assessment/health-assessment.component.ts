import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-health-assessment',
  templateUrl: './health-assessment.component.html',
  styleUrls: ['./health-assessment.component.scss']
})
export class HealthAssessmentComponent extends IntakeComponentBase implements OnInit {

//Drop down values for enumeration
  healthAssessmentOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheckRecord?.id],
      visaCountryId: [this.visaCheckRecord?.country?.id],
      visaHealthAssessment: [this.visaCheckRecord?.healthAssessment],
      visaHealthAssessmentNotes: [this.visaCheckRecord?.healthAssessmentNotes],
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

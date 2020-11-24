import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {CandidateVisaCheck, YesNo} from '../../../../model/candidate';
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
      visaId: [this.myRecord?.id],
      visaCountryId: [this.myRecord?.country?.id],
      healthAssessment: [this.myRecord?.eligibility],
      visaAssessmentNotes: [this.myRecord?.assessmentNotes],
    });
  }

  private get myRecord(): CandidateVisaCheck {
    return this.candidateIntakeData.candidateVisaChecks ?
      this.candidateIntakeData.candidateVisaChecks[this.myRecordIndex]
      : null;
  }

}

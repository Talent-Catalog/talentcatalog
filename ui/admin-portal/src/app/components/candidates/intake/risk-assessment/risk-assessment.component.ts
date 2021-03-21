import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {CandidateVisa, RiskAssessment} from "../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-risk-assessment',
  templateUrl: './risk-assessment.component.html',
  styleUrls: ['./risk-assessment.component.scss']
})
export class RiskAssessmentComponent extends IntakeComponentBase implements OnInit {

//Drop down values for enumeration
  riskAssessmentOptions: EnumOption[] = enumOptions(RiskAssessment);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.myRecord?.id],
      visaCountryId: [this.myRecord?.country?.id],
      riskAssessment: [this.myRecord?.eligibility],
      visaAssessmentNotes: [this.myRecord?.assessmentNotes],
    });
  }

  private get myRecord(): CandidateVisa {
    return this.candidateIntakeData?.candidateVisaChecks ?
      this.candidateIntakeData.candidateVisaChecks[this.myRecordIndex]
      : null;
  }

}

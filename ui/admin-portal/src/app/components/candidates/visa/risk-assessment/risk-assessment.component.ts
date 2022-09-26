import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {RiskLevel} from "../../../../model/candidate";
import {UntypedFormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-risk-assessment',
  templateUrl: './risk-assessment.component.html',
  styleUrls: ['./risk-assessment.component.scss']
})
export class RiskAssessmentComponent extends IntakeComponentBase implements OnInit {

//Drop down values for enumeration
  riskLevelOptions: EnumOption[] = enumOptions(RiskLevel);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheckRecord?.id],
      visaCountryId: [this.visaCheckRecord?.country?.id],
      visaOverallRisk: [this.visaCheckRecord?.overallRisk],
      visaOverallRiskNotes: [this.visaCheckRecord?.overallRiskNotes],
    });
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.visaOverallRisk) {
      if (this.form.value.visaOverallRisk === 'Low') {
        found = true
      }
      if (this.form.value.visaOverallRisk === 'Medium') {
        found = true
      }
      if (this.form.value.visaOverallRisk === 'High') {
        found = true
      }
    }
    return found;
  }

}

import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {RiskLevel} from "../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {VisaCheckComponentBase} from "../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-risk-assessment',
  templateUrl: './risk-assessment.component.html',
  styleUrls: ['./risk-assessment.component.scss']
})
export class RiskAssessmentComponent extends VisaCheckComponentBase implements OnInit {

//Drop down values for enumeration
  riskLevelOptions: EnumOption[] = enumOptions(RiskLevel);

  constructor(fb: FormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheck?.id],
      visaCountryId: [this.visaCheck?.country?.id],
      visaOverallRisk: [this.visaCheck?.overallRisk],
      visaOverallRiskNotes: [this.visaCheck?.overallRiskNotes],
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

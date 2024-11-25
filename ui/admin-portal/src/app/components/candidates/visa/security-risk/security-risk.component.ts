import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";
import {UntypedFormBuilder} from "@angular/forms";
import {VisaCheckComponentBase} from "../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-security-risk',
  templateUrl: './security-risk.component.html',
  styleUrls: ['./security-risk.component.scss']
})
export class SecurityRiskComponent extends VisaCheckComponentBase implements OnInit {

//Drop down values for enumeration
  securityRiskOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheck?.id],
      visaCountryId: [this.visaCheck?.country?.id],
      visaSecurityRisk: [this.visaCheck?.securityRisk],
      visaSecurityRiskNotes: [this.visaCheck?.securityRiskNotes],
    });
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.visaSecurityRisk) {
      if (this.form.value.visaSecurityRisk === 'Yes') {
        found = true
      }
      if (this.form.value.visaSecurityRisk === 'No') {
        found = true
      }
    }
    return found;
  }
}

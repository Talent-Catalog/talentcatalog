import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";
import {UntypedFormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-security-risk',
  templateUrl: './security-risk.component.html',
  styleUrls: ['./security-risk.component.scss']
})
export class SecurityRiskComponent extends IntakeComponentBase implements OnInit {

//Drop down values for enumeration
  securityRiskOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheckRecord?.id],
      visaCountryId: [this.visaCheckRecord?.country?.id],
      visaSecurityRisk: [this.visaCheckRecord?.securityRisk],
      visaSecurityRiskNotes: [this.visaCheckRecord?.securityRiskNotes],
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

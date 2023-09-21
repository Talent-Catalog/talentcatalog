import {Component, OnInit} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";
import {VisaCheckComponentBase} from "../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-int-protection',
  templateUrl: './int-protection.component.html',
  styleUrls: ['./int-protection.component.scss']
})
export class IntProtectionComponent extends VisaCheckComponentBase implements OnInit {
  public visaProtectionOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheck?.id],
      visaCountryId: [this.visaCheck?.country.id],
      visaProtection: [this.visaCheck?.protection],
      visaProtectionGrounds: [this.visaCheck?.protectionGrounds],
    });
  }

}

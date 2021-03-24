import {Component, OnInit} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";

@Component({
  selector: 'app-int-protection',
  templateUrl: './int-protection.component.html',
  styleUrls: ['./int-protection.component.scss']
})
export class IntProtectionComponent extends IntakeComponentBase implements OnInit {
  public visaProtectionOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheckRecord?.id],
      visaCountryId: [this.visaCheckRecord?.country.id],
      visaProtection: [this.visaCheckRecord?.protection],
      visaProtectionGrounds: [this.visaCheckRecord?.protectionGrounds],
    });
  }

}

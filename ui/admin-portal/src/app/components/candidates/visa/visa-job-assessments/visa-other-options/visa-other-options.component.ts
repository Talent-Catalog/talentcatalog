import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {OtherVisas} from '../../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-visa-other-options',
  templateUrl: './visa-other-options.component.html',
  styleUrls: ['./visa-other-options.component.scss']
})
export class VisaOtherOptionsComponent extends VisaCheckComponentBase implements OnInit {

  public visaOtherOptions: EnumOption[] = enumOptions(OtherVisas);

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobEligibleOther: [this.visaJobCheck?.eligibleOther],
      visaJobEligibleOtherNotes: [this.visaJobCheck?.eligibleOtherNotes],
    });
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.visaJobEligibleOther) {
      if (this.form.value.visaJobEligibleOther === 'NoResponse') {
        found = false;
      } else {
        found = true;
      }
    }
    return found;
  }
}

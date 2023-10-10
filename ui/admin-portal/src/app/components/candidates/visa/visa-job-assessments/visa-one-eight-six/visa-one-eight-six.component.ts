import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {YesNo} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-visa-one-eight-six',
  templateUrl: './visa-one-eight-six.component.html',
  styleUrls: ['./visa-one-eight-six.component.scss']
})
export class VisaOneEightSixComponent extends VisaCheckComponentBase implements OnInit {

  public visa186Options: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobEligible186: [this.visaJobCheck?.eligible_186],
      visaJobEligible186Notes: [this.visaJobCheck?.eligible_186_Notes],
    });
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.visaJobEligible186) {
      if (this.form.value.visaJobEligible186 === 'Yes') {
        found = true
      }
      if (this.form.value.visaJobEligible186 === 'No') {
        found = true
      }
    }
    return found;
  }
}

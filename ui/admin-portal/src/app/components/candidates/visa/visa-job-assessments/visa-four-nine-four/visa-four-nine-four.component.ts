import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {YesNo} from '../../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-visa-four-nine-four',
  templateUrl: './visa-four-nine-four.component.html',
  styleUrls: ['./visa-four-nine-four.component.scss']
})
export class VisaFourNineFourComponent extends VisaCheckComponentBase implements OnInit {
  public visa494Options: EnumOption[] = enumOptions(YesNo);

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobEligible494: [this.visaJobCheck?.eligible_494],
      visaJobEligible494Notes: [this.visaJobCheck?.eligible_494_Notes],
    });
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.visaJobEligible494) {
      if (this.form.value.visaJobEligible494 === 'Yes') {
        found = true
      }
      if (this.form.value.visaJobEligible494 === 'No') {
        found = true
      }
    }
    return found;
  }
}

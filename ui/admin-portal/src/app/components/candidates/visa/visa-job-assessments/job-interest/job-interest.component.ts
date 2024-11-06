import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {YesNo} from '../../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-job-interest',
  templateUrl: './job-interest.component.html',
  styleUrls: ['./job-interest.component.scss']
})
export class JobInterestComponent extends VisaCheckComponentBase implements OnInit {

  public jobInterestOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobInterest: [this.visaJobCheck?.interest],
      visaJobInterestNotes: [this.visaJobCheck?.interestNotes],
    });
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.visaJobInterest) {
      if (this.form.value.visaJobInterest === 'Yes') {
        found = true
      }
      if (this.form.value.visaJobInterest === 'No') {
        found = true
      }
    }
    return found;
  }
}

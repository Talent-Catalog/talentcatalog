import {Component, Input, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {CandidateVisaJobCheck, YesNo} from '../../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-job-interest',
  templateUrl: './job-interest.component.html',
  styleUrls: ['./job-interest.component.scss']
})
export class JobInterestComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedIndex: number;
  @Input() selectedJobCheck: CandidateVisaJobCheck;
  public jobInterestOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.selectedJobCheck?.id],
      visaJobInterest: [this.selectedJobCheck?.interest],
      visaJobInterestNotes: [this.selectedJobCheck?.interestNotes],
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

import {Component, Input, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {CandidateVisaJobCheck, YesNo} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-visa-four-nine-four',
  templateUrl: './visa-four-nine-four.component.html',
  styleUrls: ['./visa-four-nine-four.component.scss']
})
export class VisaFourNineFourComponent extends IntakeComponentBase implements OnInit {
  @Input() selectedJobCheck: CandidateVisaJobCheck;
  public visa494Options: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.selectedJobCheck?.id],
      visaJobEligible494: [this.selectedJobCheck?.eligible_494],
      visaJobEligible494Notes: [this.selectedJobCheck?.eligible_494_Notes],
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

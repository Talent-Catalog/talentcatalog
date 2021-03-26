import {Component, Input, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {CandidateVisaJobCheck, YesNo} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-visa-one-eight-six',
  templateUrl: './visa-one-eight-six.component.html',
  styleUrls: ['./visa-one-eight-six.component.scss']
})
export class VisaOneEightSixComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedIndex: number;
  @Input() selectedJobCheck: CandidateVisaJobCheck;
  public visa186Options: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.selectedJobCheck?.id],
      visaJobEligible186: [this.selectedJobCheck?.eligible_186],
      visaJobEligible186Notes: [this.selectedJobCheck?.eligible_186_Notes],
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

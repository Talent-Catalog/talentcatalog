import {Component, Input, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {CandidateVisaJobCheck, OtherVisas} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-visa-other-options',
  templateUrl: './visa-other-options.component.html',
  styleUrls: ['./visa-other-options.component.scss']
})
export class VisaOtherOptionsComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedIndex: number;
  @Input() selectedJobCheck: CandidateVisaJobCheck;
  public visaOtherOptions: EnumOption[] = enumOptions(OtherVisas);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.selectedJobCheck?.id],
      visaJobEligibleOther: [this.selectedJobCheck?.eligibleOther],
      visaJobEligibleOtherNotes: [this.selectedJobCheck?.eligibleOtherNotes],
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

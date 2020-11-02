import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {LeftHomeReason} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-left-home-reason',
  templateUrl: './left-home-reason.component.html',
  styleUrls: ['./left-home-reason.component.scss']
})
export class LeftHomeReasonComponent extends IntakeComponentBase implements OnInit {

  public leftHomeReasonOptions: EnumOption[] = enumOptions(LeftHomeReason);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService)
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      leftHomeReason: [this.candidateIntakeData?.leftHomeReason],
      leftHomeOther: [this.candidateIntakeData?.leftHomeOther]
    });
  }

  get leftHomeReason(): string {
    return this.form.value?.leftHomeReason;
  }

}

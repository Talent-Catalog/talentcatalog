import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNoUnsure} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-return-home-future',
  templateUrl: './return-home-future.component.html',
  styleUrls: ['./return-home-future.component.scss']
})
export class ReturnHomeFutureComponent extends IntakeComponentBase implements OnInit {

  public returnHomeFutureOptions: EnumOption[] = enumOptions(YesNoUnsure);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService)
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      returnHomeFuture: [this.candidateIntakeData?.returnHomeFuture],
      returnHomeWhen: [this.candidateIntakeData?.returnHomeWhen]
    });
  }

  get returnHomeFuture(): string {
    return this.form.value?.returnHomeFuture;
  }
}

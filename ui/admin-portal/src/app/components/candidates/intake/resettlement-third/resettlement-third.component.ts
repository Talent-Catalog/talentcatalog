import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-resettlement-third',
  templateUrl: './resettlement-third.component.html',
  styleUrls: ['./resettlement-third.component.scss']
})
export class ResettlementThirdComponent extends IntakeComponentBase implements OnInit {

  public resettlementThirdOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      resettleThird: [this.candidateIntakeData?.resettleThird],
      resettleThirdStatus: [this.candidateIntakeData?.resettleThirdStatus],
    });
  }

  get resettlementThird(): string {
    return this.form.value?.resettleThird;
  }
}

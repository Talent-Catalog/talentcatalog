import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNoUnsure} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-return-home-safe',
  templateUrl: './return-home-safe.component.html',
  styleUrls: ['./return-home-safe.component.scss']
})
export class ReturnHomeSafeComponent extends IntakeComponentBase implements OnInit {

  public homeSafeOptions: EnumOption[] = enumOptions(YesNoUnsure);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      returnHomeSafe: [this.candidateIntakeData?.returnHomeSafe]
    });
  }

}

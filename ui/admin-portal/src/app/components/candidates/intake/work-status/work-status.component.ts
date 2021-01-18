import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo, YesNoUnemployed} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-work-status',
  templateUrl: './work-status.component.html',
  styleUrls: ['./work-status.component.scss']
})
export class WorkStatusComponent extends IntakeComponentBase implements OnInit {

  public workDesiredOptions: EnumOption[] = enumOptions(YesNoUnemployed);
  public workLegallyOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      workDesired: [this.candidateIntakeData?.workDesired],
      workLegally: [this.candidateIntakeData?.workLegally],
    });
  }

  get workDesired(): string {
    return this.form.value?.workDesired;
  }

}

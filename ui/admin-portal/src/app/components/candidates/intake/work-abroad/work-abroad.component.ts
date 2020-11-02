import {Component, Input, OnInit} from '@angular/core';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {Country} from '../../../../model/country';

@Component({
  selector: 'app-work-abroad',
  templateUrl: './work-abroad.component.html',
  styleUrls: ['./work-abroad.component.scss']
})
export class WorkAbroadComponent extends IntakeComponentBase implements OnInit {

  @Input() countries: Country[];

  public workAbroadOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      workAbroad: [this.candidateIntakeData?.workAbroad],
      workAbroadLocId: [this.candidateIntakeData?.workAbroadLoc?.id],
      workAbroadYrs: [this.candidateIntakeData?.workAbroadYrs],
    });
  }
  get workAbroad(): string {
    return this.form.value?.workAbroad;
  }
}

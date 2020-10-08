import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {WorkPermitValidity, YesNoUnsure} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-work-permit',
  templateUrl: './work-permit.component.html',
  styleUrls: ['./work-permit.component.scss']
})
export class WorkPermitComponent extends IntakeComponentBase implements OnInit {

  public workPermitOptions: EnumOption[] = enumOptions(WorkPermitValidity);
  public workPermitDesiredOptions: EnumOption[] = enumOptions(YesNoUnsure);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      workPermit: [this.candidateIntakeData?.workPermit],
      workPermitDesired: [this.candidateIntakeData?.workPermitDesired]
    });
  }

  get workPermit(): string {
    return this.form.value?.workPermit;
  }

}

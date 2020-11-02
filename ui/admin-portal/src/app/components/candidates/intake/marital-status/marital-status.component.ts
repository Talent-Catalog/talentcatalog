import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {MaritalStatus, YesNoUnsure} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-marital-status',
  templateUrl: './marital-status.component.html',
  styleUrls: ['./marital-status.component.scss']
})
export class MaritalStatusComponent extends IntakeComponentBase implements OnInit {

  public maritalStatusOptions: EnumOption[] = enumOptions(MaritalStatus);
  public partnerTBBOptions: EnumOption[] = enumOptions(YesNoUnsure
  );

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      maritalStatus: [this.candidateIntakeData?.maritalStatus],
      partnerTBB: [this.candidateIntakeData?.partnerTBB],
    });
  }

  get maritalStatus() {
    return this.form.value?.maritalStatus;
  }

  get isMarriedEngaged(): boolean {
    let found: boolean = false;
    if (this.maritalStatus) {
      if (this.maritalStatus === 'Engaged') {
        found = true;
      } else if (this.maritalStatus === 'Married') {
        found = true;
      }
    }
    return found;
  }

}

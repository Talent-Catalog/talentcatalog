import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {UnhcrStatus, YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-registration-unhcr',
  templateUrl: './registration-unhcr.component.html',
  styleUrls: ['./registration-unhcr.component.scss']
})
export class RegistrationUnhcrComponent extends IntakeComponentBase implements OnInit {

  public unhcrStatusOptions: EnumOption[] = enumOptions(UnhcrStatus);
  public unhcrPermissionOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      unhcrStatus: [this.candidateIntakeData?.unhcrStatus],
      unhcrOldStatus: [this.candidateIntakeData?.unhcrOldStatus],
      unhcrNumber: [this.candidateIntakeData?.unhcrNumber],
      unhcrFile: [this.candidateIntakeData?.unhcrFile],
      unhcrNotes: [this.candidateIntakeData?.unhcrNotes],
      unhcrPermission: [this.candidateIntakeData?.unhcrPermission],
    });
  }

  get unhcrStatus(): string {
    return this.form.value?.unhcrStatus;
  }

  showUnhcrNumber(): boolean {
    if (this.unhcrStatus === 'MandateRefugee' ||
        this.unhcrStatus === 'RegisteredAsylum' ||
        this.unhcrStatus === 'RegisteredStateless') {
      return true;
    } else {
      return false;
    }
  }

}

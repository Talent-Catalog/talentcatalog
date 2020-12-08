import {Component, Input} from '@angular/core';
import {IntakeComponentTabBase} from '../../../../../util/intake/IntakeComponentTabBase';
import {CandidateVisaCheck} from '../../../../../../model/candidate';
import {FormGroup} from '@angular/forms';

@Component({
  selector: 'app-visa-check-ca',
  templateUrl: './visa-check-ca.component.html',
  styleUrls: ['./visa-check-ca.component.scss']
})
export class VisaCheckCaComponent extends IntakeComponentTabBase {
  @Input() selectedIndex: number;
  @Input() visaRecord: CandidateVisaCheck;
  form: FormGroup;

  protected onDataLoaded(init: boolean) {
    this.form = this.fb.group({
      jobName: [this.jobIndex]
    });
  }

  private get myRecord(): CandidateVisaCheck {
    return this.candidateIntakeData.candidateVisaChecks ?
      this.candidateIntakeData.candidateVisaChecks[this.selectedIndex]
      : null;
  }
}

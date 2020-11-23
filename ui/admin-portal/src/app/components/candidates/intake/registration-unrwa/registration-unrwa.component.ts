import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {UnrwaStatus} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-registration-unrwa',
  templateUrl: './registration-unrwa.component.html',
  styleUrls: ['./registration-unrwa.component.scss']
})
export class RegistrationUnrwaComponent extends IntakeComponentBase implements OnInit {

  public unrwaStatusOptions: EnumOption[] = enumOptions(UnrwaStatus);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      unrwaStatus: [this.candidateIntakeData?.unrwaStatus],
      unrwaNumber: [this.candidateIntakeData?.unrwaNumber],
      unrwaNotes: [this.candidateIntakeData?.unrwaNotes],
    });
  }

  get unrwaStatus(): string {
    return this.form.value?.unrwaStatus;
  }

  showUnrwaNumber(): boolean {
    return this.unrwaStatus === 'Registered';
  }

}

import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNoUnsure} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-registration-unrwa',
  templateUrl: './registration-unrwa.component.html',
  styleUrls: ['./registration-unrwa.component.scss']
})
export class RegistrationUnrwaComponent extends IntakeComponentBase implements OnInit {

  public unrwaRegisteredOptions: EnumOption[] = enumOptions(YesNoUnsure);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      unrwaRegistered: [this.candidateIntakeData?.unrwaRegistered],
      unrwaWasRegistered: [this.candidateIntakeData?.unrwaWasRegistered],
      unrwaNumber: [this.candidateIntakeData?.unrwaNumber],
      unrwaNotes: [this.candidateIntakeData?.unrwaNotes],
    });
  }

  get unrwaRegistered(): string {
    return this.form.value?.unrwaRegistered;
  }

  showUnrwaNumber(): boolean {
    return this.unrwaRegistered === 'Yes' ? true : false;
  }

}

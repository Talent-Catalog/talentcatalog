import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-dependants',
  templateUrl: './dependants.component.html',
  styleUrls: ['./dependants.component.scss']
})
export class DependantsComponent extends IntakeComponentBase implements OnInit {

  public dependantsOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      dependants: [this.candidateIntakeData?.dependants],
      dependantsNotes: [this.candidateIntakeData?.dependantsNotes],
    });
  }

  get dependants(): string {
    return this.form.value?.dependants;
  }

  get hasDependants(): boolean {
    let found: boolean = false;
    if (this.form?.value) {
      found = this.form.value?.dependants > 0;
    }
    return found;
  }

}

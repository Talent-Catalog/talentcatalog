import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-family-health',
  templateUrl: './family-health.component.html',
  styleUrls: ['./family-health.component.scss']
})
export class FamilyHealthComponent extends IntakeComponentBase implements OnInit {

  public familyHealthConcernOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      familyHealthConcern: [this.candidateIntakeData?.familyHealthConcern],
      familyHealthConcernNotes: [this.candidateIntakeData?.familyHealthConcernNotes],
    });
  }

  get familyHealthConcern(): string {
    return this.form.value?.familyHealthConcern;
  }

}

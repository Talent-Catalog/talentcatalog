import {Component, OnInit} from '@angular/core';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';

@Component({
  selector: 'app-family',
  templateUrl: './family.component.html',
  styleUrls: ['./family.component.scss']
})
export class FamilyComponent extends IntakeComponentBase implements OnInit {

  public familyMoveOptions: EnumOption[] = enumOptions(YesNo);
  public familyHealthConcernOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      familyMove: [this.candidateIntakeData?.familyMove],
      familyMoveNotes: [this.candidateIntakeData?.familyMoveNotes],
      familyHealthConcern: [this.candidateIntakeData?.familyHealth],
      familyHealthConcernNotes: [this.candidateIntakeData?.familyHealthNotes],
    });
  }

  get returnedHome(): string {
    return this.form.value?.returnedHome;
  }

}

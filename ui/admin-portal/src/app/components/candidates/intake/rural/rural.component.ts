import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNoUnsure} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-rural',
  templateUrl: './rural.component.html',
  styleUrls: ['./rural.component.scss']
})
export class RuralComponent extends IntakeComponentBase implements OnInit {

  public intRecruitRuralOptions: EnumOption[] = enumOptions(YesNoUnsure);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      intRecruitRural: [this.candidateIntakeData?.intRecruitRural],
      intRecruitRuralNotes: [this.candidateIntakeData?.intRecruitRuralNotes],
    });
  }

  get ruralInterest(): boolean {
    let interested: boolean;
    if (this.form.value.intRecruitRural) {
      interested = this.form.value.intRecruitRural === 'Yes' || this.form.value.intRecruitRural === 'Unsure';
    }
    return interested;
  }
}

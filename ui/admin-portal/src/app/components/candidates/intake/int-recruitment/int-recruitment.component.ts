import {Component, OnInit} from '@angular/core';
import {enumMultiSelectSettings, EnumOption, enumOptions} from '../../../../util/enum';
import {IntRecruitReason, YesNoUnsure} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {IDropdownSettings} from 'ng-multiselect-dropdown';

@Component({
  selector: 'app-int-recruitment',
  templateUrl: './int-recruitment.component.html',
  styleUrls: ['./int-recruitment.component.scss']
})
export class IntRecruitmentComponent extends IntakeComponentBase implements OnInit {

  public dropdownSettings: IDropdownSettings = enumMultiSelectSettings;
  public intRecruitReasonOptions: EnumOption[] = enumOptions(IntRecruitReason);
  public intRecruitRuralOptions: EnumOption[] = enumOptions(YesNoUnsure);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      intRecruitReasons: [this.candidateIntakeData?.intRecruitReasons],
      intRecruitRural: [this.candidateIntakeData?.intRecruitRural],
    });
  }

}

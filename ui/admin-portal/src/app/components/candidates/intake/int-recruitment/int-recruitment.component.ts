import {Component, OnInit} from '@angular/core';
import {enumKeysToEnumOptions, enumMultiSelectSettings, EnumOption, enumOptions} from '../../../../util/enum';
import {IntRecruitReason} from '../../../../model/candidate';
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

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    const options: EnumOption[] =
      enumKeysToEnumOptions(this.candidateIntakeData?.intRecruitReasons, IntRecruitReason);
    this.form = this.fb.group({
      intRecruitReasons: [options],
    });
  }

}

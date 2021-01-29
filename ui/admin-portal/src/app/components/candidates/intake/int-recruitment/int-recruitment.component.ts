/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

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

  get hasOther(): boolean {
    let found: boolean;
    found = this.form.value?.intRecruitReasons?.includes('Other');
    return found;
  }

}


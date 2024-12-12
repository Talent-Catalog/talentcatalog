/*
 * Copyright (c) 2024 Talent Catalog.
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
import {enumKeysToEnumOptions, EnumOption, enumOptions} from '../../../../util/enum';
import {IntRecruitReason} from '../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-int-recruitment',
  templateUrl: './int-recruitment.component.html',
  styleUrls: ['./int-recruitment.component.scss']
})
export class IntRecruitmentComponent extends IntakeComponentBase implements OnInit {

  public intRecruitReasonOptions: EnumOption[] = enumOptions(IntRecruitReason);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    const options: EnumOption[] =
      enumKeysToEnumOptions(this.candidateIntakeData?.intRecruitReasons, IntRecruitReason);
    this.form = this.fb.group({
      intRecruitReasons: [options],
      intRecruitOther: [this.candidateIntakeData?.intRecruitOther],
    });
  }

  get hasOther(): boolean {
    let found: boolean;
    // Check if reasons is an array of objects or strings (changes the way we handle the search for Other)
    if (this.form?.value?.intRecruitReasons?.some((r: EnumOption) => r.key)) {
      found = this.form?.value?.intRecruitReasons?.find((r: EnumOption) => r.key === 'Other');
    } else {
      found = this.form?.value?.intRecruitReasons?.includes('Other')
    }
    return found;
  }

}


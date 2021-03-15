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
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {
  enumKeysToEnumOptions,
  enumMultiSelectSettings,
  EnumOption,
  enumOptions
} from "../../../../util/enum";
import {VisaIssue} from "../../../../model/candidate";
import {IDropdownSettings} from "ng-multiselect-dropdown";

@Component({
  selector: 'app-visa-issues',
  templateUrl: './visa-issues.component.html',
  styleUrls: ['./visa-issues.component.scss']
})
export class VisaIssuesComponent extends IntakeComponentBase implements OnInit {

  public dropdownSettings: IDropdownSettings = enumMultiSelectSettings;
  public visaIssueOptions: EnumOption[] = enumOptions(VisaIssue);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService)
  }

  ngOnInit(): void {
    const options: EnumOption[] =
      enumKeysToEnumOptions(this.candidateIntakeData?.visaIssues, VisaIssue);
    this.form = this.fb.group({
      visaIssues: [options],
      visaIssuesNotes: [this.candidateIntakeData?.visaIssuesNotes],
    });
  }

  get haveIssues(): boolean {
    return this.form.value.visaIssues?.length > 0;
  }
}

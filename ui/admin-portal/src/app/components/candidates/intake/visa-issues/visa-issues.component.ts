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
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";
import {UntypedFormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNoUnsure} from "../../../../model/candidate";

@Component({
  selector: 'app-visa-issues',
  templateUrl: './visa-issues.component.html',
  styleUrls: ['./visa-issues.component.scss']
})
export class VisaIssuesComponent extends IntakeComponentBase implements OnInit {

  public visaIssueOptions: EnumOption[] = enumOptions(YesNoUnsure);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService)
  }

  ngOnInit(): void {
    //const options: EnumOption[] = enumKeysToEnumOptions(this.candidateIntakeData?.visaIssues, VisaIssue);
    this.form = this.fb.group({
      visaIssues: [{value: this.candidateIntakeData?.visaIssues, disabled: !this.editable}],
      visaIssuesNotes: [{value: this.candidateIntakeData?.visaIssuesNotes, disabled: !this.editable}],
    });
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.visaIssues) {
      if (this.form.value.visaIssues === 'Yes') {
        found = true
      }
      if (this.form.value.visaIssues === 'No') {
        found = true
      }
      if (this.form.value.visaIssues === 'Unsure') {
        found = true
      }
    }
    return found;
  }
}

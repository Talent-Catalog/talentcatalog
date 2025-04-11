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
import {YesNo} from "../../../../model/candidate";

@Component({
  selector: 'app-health-issues',
  templateUrl: './health-issues.component.html',
  styleUrls: ['./health-issues.component.scss']
})
export class HealthIssuesComponent extends IntakeComponentBase implements OnInit {

  public healthIssuesOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      healthIssues: [{value: this.candidateIntakeData?.healthIssues, disabled: !this.editable}],
      healthIssuesNotes: [{value: this.candidateIntakeData?.healthIssuesNotes, disabled: !this.editable}],
    });
    this.updateDataOnFieldChange("healthIssues");
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.healthIssues) {
      if (this.form.value.healthIssues === 'Yes') {
        found = true
      }
      if (this.form.value.healthIssues === 'No') {
        found = true
      }
    }
    return found;
  }

}

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
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {VisaCheckComponentBase} from "../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-health-assessment',
  templateUrl: './health-assessment.component.html',
  styleUrls: ['./health-assessment.component.scss']
})
export class HealthAssessmentComponent extends VisaCheckComponentBase implements OnInit {

//Drop down values for enumeration
  healthAssessmentOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheck?.id],
      visaCountryId: [this.visaCheck?.country?.id],
      visaHealthAssessment: [this.visaCheck?.healthAssessment],
      visaHealthAssessmentNotes: [this.visaCheck?.healthAssessmentNotes],
    });
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.visaHealthAssessment) {
      if (this.form.value.visaHealthAssessment === 'Yes') {
        found = true
      }
      if (this.form.value.visaHealthAssessment === 'No') {
        found = true
      }
    }
    return found;
  }

}

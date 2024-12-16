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
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNoUnsure} from "../../../../model/candidate";
import {UntypedFormBuilder} from "@angular/forms";
import {VisaCheckComponentBase} from "../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-pathway-assessment',
  templateUrl: './pathway-assessment.component.html',
  styleUrls: ['./pathway-assessment.component.scss']
})
export class PathwayAssessmentComponent extends VisaCheckComponentBase implements OnInit {

//Drop down values for enumeration
  assessmentOptions: EnumOption[] = enumOptions(YesNoUnsure);

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheck?.id],
      visaCountryId: [this.visaCheck?.country?.id],
      visaPathwayAssessment: [this.visaCheck?.pathwayAssessment],
      visaPathwayAssessmentNotes: [this.visaCheck?.pathwayAssessmentNotes],
    });
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.visaPathwayAssessment) {
      if (this.form.value.visaPathwayAssessment === 'Yes') {
        found = true
      }
      if (this.form.value.visaPathwayAssessment === 'No') {
        found = true
      }
      if (this.form.value.visaPathwayAssessment === 'Unsure') {
        found = true
      }
    }
    return found;
  }

}

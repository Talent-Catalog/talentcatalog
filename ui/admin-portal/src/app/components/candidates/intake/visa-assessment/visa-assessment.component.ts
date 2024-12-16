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

import {Component, Input, OnInit} from '@angular/core';
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";
import {UntypedFormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {CandidateVisaJobCheck, VisaEligibility} from "../../../../model/candidate";
import {EnumOption, enumOptions} from "../../../../util/enum";

@Component({
  selector: 'app-visa-assessment',
  templateUrl: './visa-assessment.component.html',
  styleUrls: ['./visa-assessment.component.scss']
})
export class VisaAssessmentComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedJobCheck: CandidateVisaJobCheck;
  //Drop down values for enumeration
  visaEligibilityOptions: EnumOption[] = enumOptions(VisaEligibility);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.selectedJobCheck?.id],
      visaJobPutForward: [this.selectedJobCheck?.putForward],
      visaJobNotes: [this.selectedJobCheck?.notes],
    });
  }

}

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

import {Component, Input} from '@angular/core';
import {IntakeComponentTabBase} from '../../../../util/intake/IntakeComponentTabBase';
import {CandidateVisaJobCheck} from "../../../../../model/candidate";

@Component({
  selector: 'app-visa-final-assessment',
  templateUrl: './visa-final-assessment.component.html',
  styleUrls: ['./visa-final-assessment.component.scss']
})
export class VisaFinalAssessmentComponent extends IntakeComponentTabBase {
  @Input() selectedIndex: number;
  @Input() visaCheckRecord: CandidateVisaJobCheck;
  @Input() selectedJobCheck: CandidateVisaJobCheck;
}

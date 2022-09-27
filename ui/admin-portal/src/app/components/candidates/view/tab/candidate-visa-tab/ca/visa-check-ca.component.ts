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

import {Component, Input} from '@angular/core';
import {IntakeComponentTabBase} from '../../../../../util/intake/IntakeComponentTabBase';
import {CandidateVisa} from '../../../../../../model/candidate';
import {FormGroup} from '@angular/forms';

@Component({
  selector: 'app-visa-check-ca',
  templateUrl: './visa-check-ca.component.html',
  styleUrls: ['./visa-check-ca.component.scss']
})
export class VisaCheckCaComponent extends IntakeComponentTabBase {
  @Input() selectedIndex: number;
  @Input() visaRecord: CandidateVisa;
  form: FormGroup;

  private get myRecord(): CandidateVisa {
    return this.candidateIntakeData.candidateVisaChecks ?
      this.candidateIntakeData.candidateVisaChecks[this.selectedIndex]
      : null;
  }
}

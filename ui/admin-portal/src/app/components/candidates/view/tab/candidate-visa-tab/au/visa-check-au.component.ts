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

import {Component, Input, OnInit} from '@angular/core';
import {IntakeComponentTabBase} from '../../../../../util/intake/IntakeComponentTabBase';
import {CandidateVisaCheck} from '../../../../../../model/candidate';

@Component({
  selector: 'app-visa-check-au',
  templateUrl: './visa-check-au.component.html',
  styleUrls: ['./visa-check-au.component.scss']
})
export class VisaCheckAuComponent extends IntakeComponentTabBase implements OnInit {
  @Input() selectedIndex: number;
  @Input() visaRecord: CandidateVisaCheck;

  ngOnInit() {
    // this.form = this.fb.group({
    //   dependantId: [this.myRecord?.id],
    //   dependantRelation: [this.myRecord?.relation],
    //   dependantDob: [this.myRecord?.dob],
    //   dependantName: [this.myRecord?.name],
    //   dependantRegistered: [this.myRecord?.registered],
    //   dependantHealthConcerns: [this.myRecord?.healthConcern],
    //   dependantNotes: [this.myRecord?.notes],
    // });
  }

  private get myRecord(): CandidateVisaCheck {
    return this.candidateIntakeData.candidateVisaChecks ?
      this.candidateIntakeData.candidateVisaChecks[this.selectedIndex]
      : null;
  }
}

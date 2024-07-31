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
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {AvailImmediateReason, YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';

@Component({
  selector: 'app-avail-immediate',
  templateUrl: './avail-immediate.component.html',
  styleUrls: ['./avail-immediate.component.scss']
})

export class AvailImmediateComponent extends IntakeComponentBase implements OnInit {

  public availImmediateOptions: EnumOption[] = enumOptions(YesNo);
  public availImmediateReasonOptions: EnumOption[] = enumOptions(AvailImmediateReason);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      availImmediate: [this.candidateIntakeData?.availImmediate],
      availImmediateJobOps: [this.candidateIntakeData?.availImmediateJobOps],
      availImmediateReason: [this.candidateIntakeData?.availImmediateReason],
      availImmediateNotes: [this.candidateIntakeData?.availImmediateNotes],
    });
  }

  get availImmediateJobOps(): string {
    return this.form.value?.availImmediateJobOps;
  }

  get availImmediate(): string {
    return this.form.value?.availImmediate;
  }

}

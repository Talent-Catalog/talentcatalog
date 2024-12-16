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
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';

@Component({
  selector: 'app-work-abroad',
  templateUrl: './work-abroad.component.html',
  styleUrls: ['./work-abroad.component.scss']
})
export class WorkAbroadComponent extends IntakeComponentBase implements OnInit {

  public workAbroadOptions: EnumOption[] = enumOptions(YesNo);
  public existingIds: number[];

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {

    this.form = this.fb.group({
      workAbroad: [this.candidateIntakeData?.workAbroad],
      workAbroadNotes: [this.candidateIntakeData?.workAbroadNotes],
    });
  }

  get workAbroad(): string {
    return this.form.value?.workAbroad;
  }

}

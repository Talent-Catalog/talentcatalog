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
import {UntypedFormBuilder} from '@angular/forms';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {CandidateService} from '../../../../services/candidate.service';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';

//todo Is this component used anywhere?

@Component({
  selector: 'app-returned-home',
  templateUrl: './returned-home.component.html',
  styleUrls: ['./returned-home.component.scss']
})
export class ReturnedHomeComponent extends IntakeComponentBase implements OnInit {

  public returnedHomeOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      returnedHome: [this.candidateIntakeData?.returnedHome],
      returnedHomeReason: [this.candidateIntakeData?.returnedHomeReason],
      returnedHomeReasonNo: [this.candidateIntakeData?.returnedHomeReasonNo],
    });
  }

  get returnedHome(): string {
    return this.form.value?.returnedHome;
  }

}

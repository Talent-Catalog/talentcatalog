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
import {YesNo, YesNoUnsure} from '../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-military-service',
  templateUrl: './military-service.component.html',
  styleUrls: ['./military-service.component.scss']
})
export class MilitaryServiceComponent extends IntakeComponentBase implements OnInit {

  public militaryServiceOptions: EnumOption[] = enumOptions(YesNo);
  public militaryWantedOptions: EnumOption[] = enumOptions(YesNoUnsure);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      militaryService: [{value: this.candidateIntakeData?.militaryService, disabled: !this.editable}],
      militaryWanted: [{value: this.candidateIntakeData?.militaryWanted, disabled: !this.editable}],
      militaryNotes: [{value: this.candidateIntakeData?.militaryNotes, disabled: !this.editable}],
      militaryStart: [{value: this.candidateIntakeData?.militaryStart, disabled: !this.editable}],
      militaryEnd: [{value: this.candidateIntakeData?.militaryEnd, disabled: !this.editable}],
    });
  }

  get served(): boolean {
    return this.form?.value?.militaryService == 'Yes'
  }

  get serviceSelected(): boolean {
    return this.form?.value?.militaryService == 'Yes' || this.form?.value?.militaryService == 'No'
  }
}

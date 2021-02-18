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
import {EnumOption, enumOptions} from '../../../../util/enum';
import {UnhcrStatus, YesNo, YesNoUnsure} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-registration-unhcr',
  templateUrl: './registration-unhcr.component.html',
  styleUrls: ['./registration-unhcr.component.scss']
})
export class RegistrationUnhcrComponent extends IntakeComponentBase implements OnInit {

  @Input() showAll: boolean = true;

  public unhcrRegisteredOptions: EnumOption[] = enumOptions(YesNoUnsure);
  public unhcrStatusOptions: EnumOption[] = enumOptions(UnhcrStatus);
  public unhcrPermissionOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      unhcrRegistered: [this.candidateIntakeData?.unhcrRegistered],
      unhcrStatus: [this.candidateIntakeData?.unhcrStatus],
      unhcrOldStatus: [this.candidateIntakeData?.unhcrOldStatus],
      unhcrNumber: [this.candidateIntakeData?.unhcrNumber],
      unhcrFile: [this.candidateIntakeData?.unhcrFile],
      unhcrNotes: [this.candidateIntakeData?.unhcrNotes],
      unhcrPermission: [this.candidateIntakeData?.unhcrPermission],
    });
  }

  get unhcrStatus(): string {
    return this.form.value?.unhcrStatus;
  }

  get unhcrRegistered(): string {
    return this.form.value?.unhcrRegistered;
  }

  showUnhcrNumber(): boolean {
    if (this.unhcrStatus === 'MandateRefugee' ||
        this.unhcrStatus === 'RegisteredAsylum' ||
        this.unhcrStatus === 'RegisteredStateless') {
      return true;
    } else {
      return false;
    }
  }

}

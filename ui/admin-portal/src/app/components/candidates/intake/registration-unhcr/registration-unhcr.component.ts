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
import {EnumOption, enumOptions} from '../../../../util/enum';
import {NotRegisteredStatus, UnhcrStatus, YesNo} from '../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-registration-unhcr',
  templateUrl: './registration-unhcr.component.html',
  styleUrls: ['./registration-unhcr.component.scss']
})
export class RegistrationUnhcrComponent extends IntakeComponentBase implements OnInit {

  @Input() showAll: boolean = true;

  public unhcrConsentOptions: EnumOption[] = enumOptions(YesNo);
  public unhcrStatusOptions: EnumOption[] = enumOptions(UnhcrStatus);
  public NotRegisteredStatusOptions: EnumOption[] = enumOptions(NotRegisteredStatus);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      unhcrStatus: [this.candidateIntakeData?.unhcrStatus],
      unhcrNumber: [this.candidateIntakeData?.unhcrNumber],
      unhcrFile: [this.candidateIntakeData?.unhcrFile],
      unhcrNotRegStatus: [this.candidateIntakeData?.unhcrNotRegStatus],
      unhcrConsent: [this.candidateIntakeData?.unhcrConsent],
      unhcrNotes: [this.candidateIntakeData?.unhcrNotes],
    });
  }

  get unhcrStatus(): string {
    return this.form.value?.unhcrStatus;
  }

  get isRegistered() {
    let registeredKeys: string[] = ["MandateRefugee", "RegisteredAsylum", "RegisteredStateless", "RegisteredStatusUnknown"];
    return registeredKeys.includes(this.unhcrStatus);
  }

  get isNotRegistered() {
    return this.unhcrStatus == "NotRegistered";
  }

  get hasNotes(): boolean {
    return !(this.unhcrStatus == null || this.unhcrStatus === 'NoResponse');
  }

}

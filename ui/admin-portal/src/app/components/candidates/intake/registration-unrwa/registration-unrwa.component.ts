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
import {UnrwaStatus, YesNoUnsure} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-registration-unrwa',
  templateUrl: './registration-unrwa.component.html',
  styleUrls: ['./registration-unrwa.component.scss']
})
export class RegistrationUnrwaComponent extends IntakeComponentBase implements OnInit {

  @Input() showAll: boolean = true;

  public unrwaRegisteredOptions: EnumOption[] = enumOptions(YesNoUnsure);
  public unrwaStatusOptions: EnumOption[] = enumOptions(UnrwaStatus);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      unrwaRegistered: [this.candidateIntakeData?.unrwaRegistered],
      unrwaStatus: [this.candidateIntakeData?.unrwaStatus],
      unrwaNumber: [this.candidateIntakeData?.unrwaNumber],
      unrwaNotes: [this.candidateIntakeData?.unrwaNotes],
    });
  }

  get unrwaStatus(): string {
    return this.form.value?.unrwaStatus;
  }

  get unrwaRegistered(): string {
    return this.form.value?.unrwaRegistered;
  }

  showUnrwaNumber(): boolean {
    return this.unrwaStatus === 'Registered';
  }

}

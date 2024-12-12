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
import {NotRegisteredStatus, YesNoUnsure} from '../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
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
  public NotRegisteredStatusOptions: EnumOption[] = enumOptions(NotRegisteredStatus);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      unrwaRegistered: [this.candidateIntakeData?.unrwaRegistered],
      unrwaNumber: [this.candidateIntakeData?.unrwaNumber],
      unrwaFile: [this.candidateIntakeData?.unrwaFile],
      unrwaNotRegStatus: [this.candidateIntakeData?.unrwaNotRegStatus],
      unrwaNotes: [this.candidateIntakeData?.unrwaNotes],
    });
  }

  get unrwaRegistered(): string {
    return this.form.value?.unrwaRegistered;
  }

  get hasNotes(): boolean {
    if (this.unrwaRegistered == null || this.unrwaRegistered === 'NoResponse') {
      return false;
    } else {
      return true;
    }
  }

}

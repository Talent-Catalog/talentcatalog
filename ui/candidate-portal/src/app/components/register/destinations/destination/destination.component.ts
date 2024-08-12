/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import {CandidateDestination, YesNoUnsureLearn} from '../../../../model/candidate';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Country} from '../../../../model/country';
import {RegistrationService} from "../../../../services/registration.service";
import {EnumOption, enumOptions} from "../../../util/enum";

@Component({
  selector: 'app-destination',
  templateUrl: './destination.component.html',
  styleUrls: ['./destination.component.scss']
})
export class DestinationComponent implements OnInit {
  @Input() candidateDestination: CandidateDestination;
  @Input() country: Country;
  @Input() saving: boolean;

  error: string;
  form: FormGroup;

  public destInterestOptions: EnumOption[] = enumOptions(YesNoUnsureLearn);

  constructor(private fb: FormBuilder,
              public registrationService: RegistrationService) {
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      destinationId: [this.candidateDestination?.id],
      destinationCountryId: [this.country.id],
      destinationInterest: [this.candidateDestination?.interest],
      destinationNotes: [this.candidateDestination?.notes],
    });
  }

  get interest(): string {
    return this.form?.value?.destinationInterest;
  }
}

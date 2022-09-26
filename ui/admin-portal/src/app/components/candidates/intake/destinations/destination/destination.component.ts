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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {
  CandidateDestination,
  FamilyRelations,
  YesNoUnsureLearn
} from '../../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';
import {Country} from '../../../../../model/country';

@Component({
  selector: 'app-destination',
  templateUrl: './destination.component.html',
  styleUrls: ['./destination.component.scss']
})
export class DestinationComponent extends IntakeComponentBase implements OnInit {
  @Input() country: Country;
  @Output() touched = new EventEmitter();

  public destInterestOptions: EnumOption[] = enumOptions(YesNoUnsureLearn);
  public destFamilyOptions: EnumOption[] = enumOptions(FamilyRelations);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      destinationId: [this.myRecord?.id],
      destinationCountryId: [this.country.id],
      destinationInterest: [this.myRecord?.interest],
      destinationFamily: [this.myRecord?.family],
      destinationLocation: [this.myRecord?.location],
      destinationNotes: [this.myRecord?.notes],
    });
  }

  private get myRecord(): CandidateDestination {
    return this.candidateIntakeData.candidateDestinations ?
      this.candidateIntakeData.candidateDestinations[this.myRecordIndex]
      : null;
  }

  get interest(): string {
    return this.form?.value?.destinationInterest;
  }

  get family(): string {
    return this.form.value?.destinationFamily;
  }

  showLocation(): boolean {
    if (this.family === 'NoRelation' || this.family === null) {
      return false;
    } else {
      return true;
    }
  }

}

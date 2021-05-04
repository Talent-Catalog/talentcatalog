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
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {generateYearArray} from '../../../../util/year-helper';
import {Country} from "../../../../model/country";

@Component({
  selector: 'app-host-entry-year',
  templateUrl: './host-entry-year.component.html',
  styleUrls: ['./host-entry-year.component.scss']
})
export class HostEntryYearComponent extends IntakeComponentBase implements OnInit {

  @Input() countries: Country[];

  public hostBornOptions: EnumOption[] = enumOptions(YesNo);

  years: number[];

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  // Year is converted to string using the ngb-date-adapter file in the util folder (see app module providers)
  ngOnInit(): void {
    this.years = generateYearArray(1950, true);
    this.form = this.fb.group({
      hostBorn: [this.candidateIntakeData?.hostBorn],
      hostEntryYear: [this.candidateIntakeData?.hostEntryYear],
      hostEntryYearNotes: [this.candidateIntakeData?.hostEntryYearNotes],
      birthCountryId: [this.candidateIntakeData?.birthCountry?.id],
      asylumYear: [this.candidateIntakeData?.asylumYear]
    });
  }

  get hostBorn(): string {
    return this.form.value?.hostBorn;
  }

  get hasNotes(): boolean {
    if (this.form.value?.hostEntryYear == null || this.form.value?.hostEntryYear === '') {
      return false;
    } else {
      return true;
    }
  }
}

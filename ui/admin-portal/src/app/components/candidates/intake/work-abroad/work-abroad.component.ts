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
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {Country} from '../../../../model/country';
import {IDropdownSettings} from 'ng-multiselect-dropdown';

@Component({
  selector: 'app-work-abroad',
  templateUrl: './work-abroad.component.html',
  styleUrls: ['./work-abroad.component.scss']
})
export class WorkAbroadComponent extends IntakeComponentBase implements OnInit {

  @Input() countries: Country[];

  /* MULTI SELECT */
  dropdownSettings: IDropdownSettings = {
    idField: 'id',
    textField: 'name',
    enableCheckAll: false,
    singleSelection: false,
    allowSearchFilter: true
  };


  public workAbroadOptions: EnumOption[] = enumOptions(YesNo);
  public selectedCountryIds: number[];
  public existingIds: number[];

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    // Get the countries from the ids
    let selectedCountries = [];
    if (this.candidateIntakeData?.workAbroadCountryIds && this.countries) {
      selectedCountries = this.countries.filter(c => this.candidateIntakeData?.workAbroadCountryIds.includes(c.id));
    } else {
      selectedCountries = [];
    }
    //this.form.controls['selectedCountries'].patchValue(selectedCountries);
    // if (this.candidateIntakeData?.workAbroadCountryIds != null) {
    //   this.existingIds = this.form.value.selectedCountries.map(c => c.id)
    // }

    this.form = this.fb.group({
      workAbroad: [this.candidateIntakeData?.workAbroad],
      workAbroadCountryIds: [],
      workAbroadNotes: [this.candidateIntakeData?.workAbroadNotes],
      // Used to display for multiselect
      selectedCountries: [selectedCountries]
    });
  }

  get workAbroad(): string {
    return this.form.value?.workAbroad;
  }

  private updateSelectedIds() {
    const ids: number[] = this.form.value.selectedCountries.map(c => c.id);
    this.form.controls['workAbroadCountryIds'].setValue(ids);
    console.log('test');
  }

  public addIds() {
    this.updateSelectedIds()
  }

  public removeIds($event) {
    this.updateSelectedIds()
  }

}

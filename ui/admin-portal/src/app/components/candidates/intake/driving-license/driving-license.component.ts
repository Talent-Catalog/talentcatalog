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
import {DrivingLicenseStatus, YesNo} from '../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {Country} from "../../../../model/country";

@Component({
  selector: 'app-driving-license',
  templateUrl: './driving-license.component.html',
  styleUrls: ['./driving-license.component.scss']
})
export class DrivingLicenseComponent extends IntakeComponentBase implements OnInit {

  @Input() countries: Country[];

  public canDriveOptions: EnumOption[] = enumOptions(YesNo);
  public drivingLicenseOptions: EnumOption[] = enumOptions(DrivingLicenseStatus);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      canDrive: [this.candidateIntakeData?.canDrive],
      drivingLicense: [this.candidateIntakeData?.drivingLicense],
      drivingLicenseExp: [this.candidateIntakeData?.drivingLicenseExp],
      drivingLicenseCountryId: [this.candidateIntakeData?.drivingLicenseCountry?.id],
    });
  }

  get canDrive(): string {
    return this.form.value?.canDrive;
  }

  get drivingLicense(): string {
    return this.form.value?.drivingLicense;
  }

  get hasDrivingLicense(): boolean {
    let found: boolean = false;
    if (this.form?.value) {
      if (this.drivingLicense === 'Valid') {
        found = true;
      } else if (this.drivingLicense === 'Expired') {
        found = true;
      }
    }
    return found;
  }

}

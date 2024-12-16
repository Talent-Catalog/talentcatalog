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
import {EnumOption, enumOptions} from "../../../../util/enum";
import {VaccinationStatus, YesNo} from "../../../../model/candidate";
import {UntypedFormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-covid-vaccination',
  templateUrl: './covid-vaccination.component.html',
  styleUrls: ['./covid-vaccination.component.scss']
})
export class CovidVaccinationComponent extends IntakeComponentBase implements OnInit {

  public vaccinationOptions: EnumOption[] = enumOptions(YesNo);
  public vaccinationStatusOptions: EnumOption[] = enumOptions(VaccinationStatus);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      covidVaccinated: [this.candidateIntakeData?.covidVaccinated],
      covidVaccinatedStatus: [this.candidateIntakeData?.covidVaccinatedStatus],
      covidVaccinatedDate: [this.candidateIntakeData?.covidVaccinatedDate],
      covidVaccineName: [this.candidateIntakeData?.covidVaccineName],
      covidVaccineNotes: [this.candidateIntakeData?.covidVaccineNotes],
    });
  }

  get covidVaccinated(): string {
    return this.form.value?.covidVaccinated;
  }

}

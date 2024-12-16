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
import {FamilyRelations} from "../../../../model/candidate";
import {UntypedFormBuilder} from "@angular/forms";
import {CandidateVisaCheckService} from "../../../../services/candidate-visa-check.service";
import {VisaCheckComponentBase} from "../../../util/intake/VisaCheckComponentBase";

@Component({
  selector: 'app-destination-family',
  templateUrl: './destination-family.component.html',
  styleUrls: ['./destination-family.component.scss']
})
export class DestinationFamilyComponent extends VisaCheckComponentBase implements OnInit {

  public destFamilyOptions: EnumOption[] = enumOptions(FamilyRelations);

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheck?.id],
      visaCountryId: [this.visaCheck?.country?.id],
      visaDestinationFamily: [this.visaCheck?.destinationFamily],
      visaDestinationFamilyLocation: [this.visaCheck?.destinationFamilyLocation],
    });
  }

  get family(): string {
    return this.form.value?.visaDestinationFamily;
  }

  showLocation(): boolean {
    return !(this.family === 'NoRelation' || this.family === null);
  }

}

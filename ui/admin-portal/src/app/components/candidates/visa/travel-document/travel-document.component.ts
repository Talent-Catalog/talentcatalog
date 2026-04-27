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
import {TravelDocumentStatus} from "../../../../model/candidate";
import {UntypedFormBuilder} from "@angular/forms";
import {VisaCheckComponentBase} from "../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-travel-document',
  templateUrl: './travel-document.component.html',
  styleUrls: ['./travel-document.component.scss']
})
export class TravelDocumentComponent extends VisaCheckComponentBase implements OnInit {

//Drop down values for enumeration
  travelDocumentOptions: EnumOption[] = enumOptions(TravelDocumentStatus);

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheck?.id],
      visaCountryId: [this.visaCheck?.country?.id],
      visaValidTravelDocs: [this.visaCheck?.validTravelDocs],
      visaValidTravelDocsNotes: [this.visaCheck?.validTravelDocsNotes],
    });
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.visaValidTravelDocs) {
      if (this.form.value.visaValidTravelDocs === 'Valid') {
        found = true
      }
      if (this.form.value.visaValidTravelDocs === 'Expired') {
        found = true
      }
      if (this.form.value.visaValidTravelDocs === 'None') {
        found = true
      }
    }
    return found;
  }

}

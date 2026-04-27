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
import {Occupation} from '../../../../../model/occupation';
import {UntypedFormBuilder} from '@angular/forms';
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-job-occupation',
  templateUrl: './job-occupation.component.html',
  styleUrls: ['./job-occupation.component.scss']
})
export class JobOccupationComponent extends VisaCheckComponentBase implements OnInit {

  @Input() occupations: Occupation[];

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobOccupationId: [this.visaJobCheck?.occupation?.id],
      visaJobOccupationNotes: [this.visaJobCheck?.occupationNotes],
    });

    this.form.controls['visaJobOccupationId']?.valueChanges.subscribe(
      change => {
        //Update my existingRecord with occupation object
          this.visaJobCheck.occupation =
            {id: change, name: null, isco08Code: null, status: null};
      }
    );
  }

  get occupationId(): number {
    return this.visaJobCheck?.occupation?.id;
  }

}

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
import {YesNoUnsure} from "../../../../model/candidate";
import {UntypedFormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-arrest-imprison',
  templateUrl: './arrest-imprison.component.html',
  styleUrls: ['./arrest-imprison.component.scss']
})
export class ArrestImprisonComponent extends IntakeComponentBase implements OnInit {

  public arrestImprisonOptions: EnumOption[] = enumOptions(YesNoUnsure);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      arrestImprison: [{value: this.candidateIntakeData?.arrestImprison, disabled: !this.editable}],
      arrestImprisonNotes: [{value: this.candidateIntakeData?.arrestImprisonNotes, disabled: !this.editable}]
    });
    this.updateDataOnFieldChange("arrestImprison");
  }

  get arrestImprison(): string {
    return this.form.value?.arrestImprison;
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.arrestImprison) {
      if (this.arrestImprison === 'Yes') {
        found = true
      }
      if (this.arrestImprison === 'Unsure') {
        found = true
      }
    }
    return found;
  }

}

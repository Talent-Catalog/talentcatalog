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

import {
  CandidateColumnSelectorComponent
} from "../../util/candidate-column-selector/candidate-column-selector.component";

import {ModalDismissReasons, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateFieldService} from "../../../services/candidate-field.service";
import {Input} from "@angular/core";
import {CandidateSource} from "../../../model/base";
import {CandidateFieldInfo} from "../../../model/candidate-field-info";

export class CandidateSourceBaseComponent {
  error: any = null;
  longFormat: boolean;

  @Input() candidateSource: CandidateSource;

  protected selectedFields: CandidateFieldInfo[] = [];

  constructor(
    protected candidateFieldService: CandidateFieldService,
    protected modalService: NgbModal
  ) {}

  onSelectColumns() {
    //Initialize with current configuration
    //Output is new configuration
    const modal = this.modalService.open(CandidateColumnSelectorComponent);
    modal.componentInstance.setSourceAndFormat(this.candidateSource, this.longFormat);

    modal.result
    .then(
      () => this.loadSelectedFields()
    )
    .catch(
      error => {
        if (error !== ModalDismissReasons.ESC) {
          this.error = error;
        }
      });
  }

  protected loadSelectedFields() {
    this.selectedFields = this.candidateFieldService
    .getCandidateSourceFields(this.candidateSource, this.longFormat);
  }

}

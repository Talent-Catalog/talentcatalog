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

import {Component} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateStatus, UpdateCandidateStatusInfo} from "../../../../model/candidate";

@Component({
  selector: 'app-edit-candidate-status',
  templateUrl: './edit-candidate-status.component.html',
  styleUrls: ['./edit-candidate-status.component.scss']
})
export class EditCandidateStatusComponent {

  candidateStatus: CandidateStatus;
  warningText: string;

  private candidateStatusInfo: UpdateCandidateStatusInfo;

  constructor(private activeModal: NgbActiveModal) {
  }

  getInitialStatus(): CandidateStatus {
    return this.candidateStatus ? this.candidateStatus : CandidateStatus.active;
  }

  onSave() {
    this.activeModal.close(this.candidateStatusInfo);
  }

  cancel() {
    this.activeModal.dismiss();
  }

  onStatusInfoUpdate(candidateStatusInfo: UpdateCandidateStatusInfo) {
    this.candidateStatusInfo = candidateStatusInfo;
  }
}

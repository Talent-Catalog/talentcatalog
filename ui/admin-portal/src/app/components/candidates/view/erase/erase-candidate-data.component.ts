/*
 * Copyright (c) 2026 Talent Catalog.
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
import {Component, Input} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {
  Candidate,
  EraseCandidateRequest,
  EraseCandidateResponse
} from '../../../../model/candidate';
import {CandidateService} from '../../../../services/candidate.service';

/**
 * Modal for full candidate data erasure.
 *
 * <p>This modal supports one action only: full candidate data erasure. There is no partial,
 * reporting, or mid-level erasure option.</p>
 *
 * <p>The modal asks the admin to:</p>
 *
 * <ol>
 *   <li>Read the erasure warning.</li>
 *   <li>Confirm by typing the candidate number and accepting that the action is permanent.</li>
 * </ol>
 *
 * <p>The actual erasure is performed by the backend endpoint:</p>
 *
 * <pre>
 * POST /api/admin/candidate/{id}/erase
 * </pre>
 */
@Component({
  selector: 'app-erase-candidate-data',
  templateUrl: './erase-candidate-data.component.html',
  styleUrls: ['./erase-candidate-data.component.scss']
})
export class EraseCandidateDataComponent {

  @Input() candidate: Candidate;

  step: number = 1;
  confirmationCandidateNumber: string = '';
  irreversibleConfirmed: boolean = false;

  erasing: boolean = false;
  error: string;

  constructor(
    private activeModal: NgbActiveModal,
    private candidateService: CandidateService
  ) {
  }

  cancel() {
    if (!this.erasing) {
      this.activeModal.dismiss();
    }
  }

  next() {
    this.error = null;
    if (this.step < 2) {
      this.step++;
    }
  }

  back() {
    this.error = null;
    if (this.step > 1) {
      this.step--;
    }
  }

  canConfirm(): boolean {
    return !this.erasing
      && this.irreversibleConfirmed
      && this.confirmationCandidateNumber?.trim() === this.candidate?.candidateNumber;
  }

  candidateName(): string {
    const firstName = this.candidate?.user?.firstName || '';
    const lastName = this.candidate?.user?.lastName || '';
    const name = `${firstName} ${lastName}`.trim();

    return name || 'this candidate';
  }

  confirm() {
    if (!this.canConfirm()) {
      this.error =
        'Please type the exact candidate number and confirm that you understand this is permanent.';
      return;
    }

    this.erasing = true;
    this.error = null;

    const request: EraseCandidateRequest = {
      confirmationCandidateNumber: this.confirmationCandidateNumber.trim()
    };

    this.candidateService.eraseCandidate(this.candidate.id, request).subscribe({
      next: (result: EraseCandidateResponse) => {
        this.erasing = false;
        this.activeModal.close(result);
      },
      error: error => {
        this.erasing = false;
        this.error = error;
      }
    });
  }

  modalActionText(): string {
    return this.step < 2 ? 'Continue' : 'Erase candidate data';
  }

  modalActionDisabled(): boolean {
    if (this.erasing) {
      return true;
    }

    if (this.step === 2) {
      return !this.canConfirm();
    }

    return false;
  }

  onModalAction() {
    if (this.step < 2) {
      this.next();
    } else {
      this.confirm();
    }
  }
}

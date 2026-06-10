/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from '../../../../../model/candidate';
import {Agreement} from '../../../../../model/agreement';
import {AgreementService} from '../../../../../services/agreement.service';

/**
 * Read-only ledger of all agreements signed by the candidate.
 *
 * This component displays the candidate's agreement history regardless of where
 * or how each agreement was accepted (e.g. via /privacy for GRN privacy policy
 * re-acceptance, via the registration flow for first-time acceptance, or inline
 * in CASI for service provider agreements).
 *
 * This component is intentionally not an acceptance workflow — it does not
 * drive or host any agreement signing flows. Acceptance flows live in the
 * feature area where the agreement is relevant (e.g. TermsComponent for /privacy,
 * CASI components for service agreements).
 *
 * Visibility: currently gated to GRN instances only (see ViewCandidateComponent.showAgreementsTab).
 *
 */
@Component({
  selector: 'app-candidate-agreements',
  templateUrl: './candidate-agreements.component.html',
  styleUrls: ['./candidate-agreements.component.scss']
})
export class CandidateAgreementsComponent implements OnInit {
  @Input() candidate: Candidate;

  agreements: Agreement[] = [];
  selectedAgreement: Agreement | null = null;
  loading: boolean;
  error: any;

  constructor(private agreementService: AgreementService) {
  }

  ngOnInit(): void {
    this.loading = true;
    this.agreementService.listMyAgreements().subscribe({
      next: agreements => {
        this.agreements = agreements;
        this.loading = false;
      },
      error: err => {
        this.error = err;
        this.loading = false;
      }
    });
  }

  isActive(agreement: Agreement): boolean {
    // Use loose equality to treat both null and undefined as active.
    return agreement.end == null;
  }

  viewAgreement(agreement: Agreement): void {
    this.selectedAgreement = agreement;
  }

  clearSelection(): void {
    this.selectedAgreement = null;
  }
}

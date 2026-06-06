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

@Component({
  selector: 'app-candidate-agreements',
  templateUrl: './candidate-agreements.component.html',
  styleUrls: ['./candidate-agreements.component.scss']
})
export class CandidateAgreementsComponent implements OnInit {
  @Input() candidate: Candidate;

  agreements: Agreement[] = [];
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
    return agreement.end === null;
  }
}

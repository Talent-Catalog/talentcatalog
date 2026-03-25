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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Candidate} from "../../../../../../model/candidate";
import {ServiceAssignment} from "../../../../../../model/services";
import {CasiPortalService} from "../../../../../../services/casi-portal.service";

/**
 * Component for displaying and managing the UNHCR help site link service for candidates.
 * This component checks if a service assignment exists for the candidate and either displays the
 * link or assigns it if not already assigned.
 *
 * @author sadatmalik
 */
@Component({
  selector: 'app-unhcr',
  templateUrl: './unhcr.component.html',
  styleUrl: './unhcr.component.scss'
})
export class UnhcrComponent implements OnInit {
  @Input() candidate!: Candidate;
  @Output() backButtonClicked = new EventEmitter<void>();

  assignment?: ServiceAssignment;
  loading: boolean;
  error: any;

  private readonly provider = 'UNHCR';
  private readonly serviceCode = 'HELP_SITE_LINK';

  constructor(private portalService: CasiPortalService) {}

  ngOnInit() {
    this.loadOrAssignLink();
  }

  get countryName(): string {
    return this.candidate?.country?.name || 'your country';
  }

  get countryIsoCode(): string {
    return (this.candidate?.country?.isoCode || '').toUpperCase();
  }

  get countryFlag(): string {
    return this.flagEmoji(this.countryIsoCode);
  }

  onBackButtonClicked() {
    this.backButtonClicked.emit();
  }

  private loadOrAssignLink() {
    this.loading = true;
    this.error = null;
    this.portalService.getAssignment(this.provider, this.serviceCode).subscribe({
      next: assignment => {
        if (assignment) {
          this.assignment = assignment;
          this.loading = false;
          return;
        }
        this.assign();
      },
      error: error => {
        this.error = error;
        this.loading = false;
      }
    });
  }

  private assign() {
    this.portalService.assign(this.provider, this.serviceCode).subscribe({
      next: assignment => {
        this.assignment = assignment;
        this.loading = false;
      },
      error: error => {
        this.error = error;
        this.loading = false;
      }
    });
  }

  private flagEmoji(isoCode: string): string {
    if (!isoCode || isoCode.length !== 2) {
      return '';
    }
    return [...isoCode]
      .map(char => String.fromCodePoint(0x1f1e6 + char.charCodeAt(0) - 65))
      .join('');
  }
}

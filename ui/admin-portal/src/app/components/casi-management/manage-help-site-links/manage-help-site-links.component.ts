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
import {SharedServiceLink} from '../../../model/shared-service-link';
import {CasiAdminService} from '../../../services/casi-admin.service';

/**
 * Admin UI for managing shared country help-site links for a CASI provider.
 * Supports listing, creating, updating, and soft-disabling links for a given
 * provider/serviceCode pair (e.g. UNHCR or PiFi HELP_SITE_LINK).
 */
@Component({
  selector: 'app-manage-help-site-links',
  templateUrl: './manage-help-site-links.component.html',
  styleUrls: ['./manage-help-site-links.component.scss']
})
export class ManageHelpSiteLinksComponent implements OnInit {
  @Input() provider!: string;
  @Input() serviceCode = 'HELP_SITE_LINK';
  @Input() title = 'Help Site Links';

  links: SharedServiceLink[] = [];
  loading = false;
  saving = false;
  error: string;

  countryIsoCode = '';
  resourceCode = '';
  editingId: number | null = null;

  constructor(private casiAdminService: CasiAdminService) {
  }

  ngOnInit(): void {
    this.loadLinks();
  }

  submit(): void {
    this.error = null;

    const countryIsoCode = this.countryIsoCode.trim().toUpperCase();
    const resourceCode = this.resourceCode.trim();
    if (!countryIsoCode || !resourceCode) {
      this.error = 'Country and link URL are required.';
      return;
    }

    this.saving = true;
    const request$ = this.editingId == null
      ? this.casiAdminService.createSharedLink(this.provider, this.serviceCode, countryIsoCode, resourceCode)
      : this.casiAdminService.updateSharedLink(this.provider, this.serviceCode, this.editingId, countryIsoCode, resourceCode);

    request$.subscribe({
      next: () => {
        this.saving = false;
        this.resetForm();
        this.loadLinks();
      },
      error: () => {
        this.saving = false;
        this.error = 'Failed to save link. Please check country code and URL.';
      }
    });
  }

  edit(link: SharedServiceLink): void {
    this.editingId = link.id;
    this.countryIsoCode = link.countryIsoCode || '';
    this.resourceCode = link.resourceCode || '';
  }

  cancelEdit(): void {
    this.resetForm();
  }

  remove(link: SharedServiceLink): void {
    if (!confirm(`Disable link for ${link.countryIsoCode}?`)) {
      return;
    }

    this.error = null;
    this.saving = true;
    this.casiAdminService.disableSharedLink(this.provider, this.serviceCode, link.id).subscribe({
      next: () => {
        this.saving = false;
        this.resetForm();
        this.loadLinks();
      },
      error: () => {
        this.saving = false;
        this.error = 'Failed to disable link.';
      }
    });
  }

  private loadLinks(): void {
    this.loading = true;
    this.casiAdminService.listSharedLinks(this.provider, this.serviceCode).subscribe({
      next: links => {
        this.links = links || [];
        this.loading = false;
      },
      error: () => {
        this.links = [];
        this.loading = false;
        this.error = 'Failed to load shared links.';
      }
    });
  }

  private resetForm(): void {
    this.editingId = null;
    this.countryIsoCode = '';
    this.resourceCode = '';
  }
}

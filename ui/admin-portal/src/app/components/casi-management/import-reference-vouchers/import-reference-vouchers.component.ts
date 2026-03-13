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
import {User} from "../../../model/user";
import {CasiAdminService} from "../../../services/casi-admin.service";
import {BaseCsvImportComponent} from "../base-csv-import.component";

/**
 * Component for importing reference vouchers via CSV in the admin portal.
 */
@Component({
  selector: 'app-import-reference-vouchers',
  templateUrl: './import-reference-vouchers.component.html',
  styleUrls: ['./import-reference-vouchers.component.scss'],
})
export class ImportReferenceVouchersComponent extends BaseCsvImportComponent {
  availableVouchersCount = 0;
  requiredColumns = ['voucher_code', 'expires_at'];
  @Input() loggedInUser!: User;

  private readonly provider = 'REFERENCE';
  private readonly serviceCode = 'VOUCHER';

  constructor(private casiAdminService: CasiAdminService) {
    super();
  }

  protected doImport(): void {
    if (!this.selectedFile) {
      this.error = 'Please select a file to import.';
      this.working = false;
      return;
    }

    this.casiAdminService.importInventory(this.provider, this.serviceCode, this.selectedFile).subscribe({
      next: () => {
        this.working = false;
        this.loadAvailableCount();
        this.csvImported = true;
        alert('CSV data imported successfully!');
      },
      error: (error) => {
        this.working = false;
        this.error = 'Failed to import the CSV file. Please try again.';
        console.error('Import error:', error);
      },
    });
  }

  protected loadAvailableCount(): void {
    this.casiAdminService.countAvailable(this.provider, this.serviceCode).subscribe(
      (response) => {
        this.availableVouchersCount = response.count;
      },
      (error) => {
        console.error('Error fetching available reference vouchers count', error);
      }
    );
  }
}

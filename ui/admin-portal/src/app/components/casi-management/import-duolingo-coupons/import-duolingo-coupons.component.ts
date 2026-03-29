/*
 * Copyright (c) 2025 Talent Catalog.
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
import {DuolingoCouponService} from "../../../services/duolingo-coupon.service";
import {User} from "../../../model/user";
import {BaseCsvImportComponent} from "../base-csv-import.component";

@Component({
  selector: 'app-import-duolingo-coupons',
  templateUrl: './import-duolingo-coupons.component.html',
  styleUrls: ['./import-duolingo-coupons.component.scss'],
})
export class ImportDuolingoCouponsComponent extends BaseCsvImportComponent {
  availableProctoredCouponsCount = 0;
  requiredColumns = ['Coupon Code', 'Expiration Date', 'Date Sent', 'Coupon Status'];
  @Input() loggedInUser!: User;

  constructor(private duolingoCouponService: DuolingoCouponService) {
    super();
  }

  protected doImport(): void {
    if (!this.selectedFile) {
      this.error = 'Please select a file to import.';
      this.working = false;
      return;
    }

    this.duolingoCouponService.importCoupons(this.selectedFile).subscribe({
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

  protected filterColumns(
    headers: string[],
    data: string[][]
  ): { headers: string[]; data: string[][] } {
    const filteredHeaders = headers.filter(
      (_, index) => index !== 1 && index !== headers.length - 1
    );
    const filteredData = data.map((row) =>
      row.filter((_, index) => index !== 1 && index !== row.length - 1)
    );
    return {headers: filteredHeaders, data: filteredData};
  }

  protected loadAvailableCount(): void {
    this.duolingoCouponService.countAvailableProctoredCoupons().subscribe(
      (response) => {
        this.availableProctoredCouponsCount = response.count;
      },
      (error) => {
        console.error('Error fetching available proctored coupons count', error);
      }
    );
  }
}

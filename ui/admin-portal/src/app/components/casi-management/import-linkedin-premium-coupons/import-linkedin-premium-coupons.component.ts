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
import {LinkedinPremiumCouponService} from "../../../services/linkedin-premium-coupon.service";
import {BaseCsvImportComponent} from "../base-csv-import.component";

@Component({
  selector: 'app-import-linkedin-premium-coupons',
  templateUrl: './import-linkedin-premium-coupons.component.html',
  styleUrls: ['./import-linkedin-premium-coupons.component.scss'],
})
export class ImportLinkedinPremiumCouponsComponent extends BaseCsvImportComponent {
  availableCouponsCount: number = 0;
  // List of required columns
  requiredColumns = ['Serial #', 'Premium Code', 'Activate by'];
  @Input() loggedInUser!: User;

  constructor(private couponService: LinkedinPremiumCouponService) {
    super();
  }

  protected doImport(): void {
    if (!this.selectedFile) {
      this.error = 'Please select a file to import.';
      this.working = false;
      return;
    }

    // Call the service to import the CSV file
    this.couponService.importCoupons(this.selectedFile).subscribe({
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
    this.couponService.countAvailableCoupons().subscribe(
      (response) => {
        this.availableCouponsCount = response.count;
      },
      (error) => {
        console.error('Error fetching available proctored coupons count', error);
      }
    );
  }
}

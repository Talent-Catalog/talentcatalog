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
  availableCouponsCount = 0;
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

import {Component, Input, OnInit} from '@angular/core';
import {User} from "../../../model/user";
import {LinkedinPremiumCouponService} from "../../../services/linkedin-premium-coupon.service";

@Component({
  selector: 'app-import-linkedin-premium-coupons',
  templateUrl: './import-linkedin-premium-coupons.component.html',
  styleUrls: ['./import-linkedin-premium-coupons.component.scss'],
})
export class ImportLinkedinPremiumCouponsComponent implements OnInit{
  error: string | null = null;
  working = false;
  csvHeaders: string[] = [];
  csvData: string[][] = [];
  paginatedData: string[][] = [];
  selectedFile: File | null = null;
  availableCouponsCount: number = 0;
  csvImported = false;
  currentPage = 1;
  pageSize = 30; // Number of items per page
  // List of required columns
  requiredColumns = ['Serial #', 'Premium Code', 'Activate by'];
  @Input() loggedInUser!: User;

  constructor(private couponService: LinkedinPremiumCouponService) {}

  ngOnInit() {
    this.getAvailableCouponsCount();
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];
      this.parseCSV(this.selectedFile);
    }
  }

  parseCSV(file: File): void {
    const reader = new FileReader();
    reader.onload = () => {
      const text = reader.result as string;
      const lines = text.split('\n').map((line) => line.trim());

      // Extract raw headers and data
      const rawHeaders = lines[0].split(',').map((header) => header.trim());
      const rawData = lines.slice(1).map((line) => line.split(','));

      // Validate headers
      const missingColumns = this.requiredColumns.filter(
        (column) => !rawHeaders.includes(column)
      );

      if (missingColumns.length) {
        this.error = `The CSV file is missing the following required columns: ${missingColumns.join(
          ', '
        )}. Please ensure the file is exported in the correct format.`;
        this.csvHeaders = [];
        this.csvData = [];
        return;
      }

      this.csvHeaders = rawHeaders;
      this.csvData = rawData;
      this.updatePaginatedData();
      this.error = null; // Clear any previous errors
    };

    reader.onerror = () => {
      this.error = 'Failed to read the file. Please try again.';
    };

    reader.readAsText(file);
  }

  importCSV(): void {
    if (!this.selectedFile) {
      this.error = 'Please select a file to import.';
      return;
    }

    this.working = true;

    // Call the service to import the CSV file
    this.couponService.importCoupons(this.selectedFile).subscribe({
      next: () => {
        this.working = false;
        this.getAvailableCouponsCount();
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

  onPageChange(page: number): void {
    this.currentPage = page;
    this.updatePaginatedData();
  }

  updatePaginatedData(): void {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.paginatedData = this.csvData.slice(startIndex, endIndex);
  }

  getAvailableCouponsCount(): void {
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

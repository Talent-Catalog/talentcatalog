import {Component, Input, OnInit} from '@angular/core';
import {DuolingoCouponService} from "../../../services/duolingo-coupon.service";
import {User} from "../../../model/user";

@Component({
  selector: 'app-import-duolingo-coupons',
  templateUrl: './import-duolingo-coupons.component.html',
  styleUrls: ['./import-duolingo-coupons.component.scss'],
})
export class ImportDuolingoCouponsComponent implements OnInit{
  error: string | null = null;
  working = false;
  csvHeaders: string[] = [];
  csvData: string[][] = [];
  paginatedData: string[][] = [];
  selectedFile: File | null = null;
  availableCouponsCount: number = 0;

  currentPage = 1;
  pageSize = 30; // Number of items per page
  // List of required columns
  requiredColumns = ['Coupon Code', 'Expiration Date', 'Date Sent', 'Coupon Status'];
  @Input() loggedInUser!: User;

  constructor(private duolingoCouponService: DuolingoCouponService) {}

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

      // Remove the second and last columns
      const filteredHeaders = rawHeaders.filter(
        (_, index) => index !== 1 && index !== rawHeaders.length - 1
      );
      const filteredData = rawData.map((row) =>
        row.filter((_, index) => index !== 1 && index !== row.length - 1)
      );

      // Assign filtered headers and data for further processing
      this.csvHeaders = filteredHeaders;
      this.csvData = filteredData;
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
    this.duolingoCouponService.importCoupons(this.selectedFile).subscribe({
      next: (response) => {
        this.working = false;
        this.getAvailableCouponsCount();
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
    this.duolingoCouponService.countAvailableCoupons().subscribe(
      (response) => {
        this.availableCouponsCount = response.count;
      },
      (error) => {
        console.error('Error fetching available coupons count', error);
      }
    );
  }
}

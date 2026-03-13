import {Component, Input, OnInit} from '@angular/core';
import {User} from "../../../model/user";
import {CasiAdminService} from "../../../services/casi-admin.service";

/**
 * Component for importing reference vouchers via CSV in the admin portal.
 * Allows admins to upload a CSV file containing voucher codes and expiration dates,
 * validates the file format, and imports the data into the system. Also displays
 * the count of available vouchers and handles pagination for large CSV files.
 *
 * Expected CSV format:
 * voucher_code,expires_at
 * REF-001,2024-12-31
 * REF-002,2025-01-31
 *
 * @author sadatmalik
 */
@Component({
  selector: 'app-import-reference-vouchers',
  templateUrl: './import-reference-vouchers.component.html',
  styleUrls: ['./import-reference-vouchers.component.scss'],
})
export class ImportReferenceVouchersComponent implements OnInit{
  error: string | null = null;
  working = false;
  csvHeaders: string[] = [];
  csvData: string[][] = [];
  paginatedData: string[][] = [];
  selectedFile: File | null = null;
  availableVouchersCount = 0;
  csvImported = false;
  currentPage = 1;
  pageSize = 30;
  requiredColumns = ['voucher_code', 'expires_at'];
  @Input() loggedInUser!: User;

  private readonly provider = 'REFERENCE';
  private readonly serviceCode = 'VOUCHER';

  constructor(private casiAdminService: CasiAdminService) {}

  ngOnInit() {
    this.getAvailableVouchersCount();
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
      const lines = text.split('\n').map((line) => line.trim()).filter(Boolean);
      const rawHeaders = lines[0].split(',').map((header) => header.trim());
      const normalizedHeaders = rawHeaders.map(header => header.toLowerCase());
      const rawData = lines.slice(1).map((line) => line.split(','));

      const missingColumns = this.requiredColumns.filter(
        (column) => !normalizedHeaders.includes(column)
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
      this.error = null;
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
    this.casiAdminService.importInventory(this.provider, this.serviceCode, this.selectedFile).subscribe({
      next: () => {
        this.working = false;
        this.getAvailableVouchersCount();
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

  getAvailableVouchersCount(): void {
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

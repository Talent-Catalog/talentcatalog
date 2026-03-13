import {OnInit} from '@angular/core';

export abstract class BaseCsvImportComponent implements OnInit {
  error: string | null = null;
  working = false;
  csvHeaders: string[] = [];
  csvData: string[][] = [];
  paginatedData: string[][] = [];
  selectedFile: File | null = null;
  csvImported = false;
  currentPage = 1;
  pageSize = 30;

  abstract requiredColumns: string[];

  ngOnInit(): void {
    this.loadAvailableCount();
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];
      this.csvImported = false;
      this.currentPage = 1;
      this.parseCSV(this.selectedFile);
    }
  }

  parseCSV(file: File): void {
    const reader = new FileReader();

    reader.onload = () => {
      const text = (reader.result as string) ?? '';
      const lines = text.split('\n').map((line) => line.trim()).filter(Boolean);
      if (!lines.length) {
        this.error = 'The CSV file is empty.';
        this.csvHeaders = [];
        this.csvData = [];
        this.paginatedData = [];
        return;
      }

      const rawHeaders = lines[0].split(',').map((header) => header.trim());
      const rawData = lines.slice(1).map((line) => line.split(','));
      const normalizedHeaders = rawHeaders.map((header) =>
        this.normalizeHeaderForComparison(header)
      );

      const missingColumns = this.requiredColumns.filter((column) => {
        const normalizedRequired = this.normalizeHeaderForComparison(column);
        return !normalizedHeaders.includes(normalizedRequired);
      });

      if (missingColumns.length) {
        this.error = `The CSV file is missing the following required columns: ${missingColumns.join(
          ', '
        )}. Please ensure the file is exported in the correct format.`;
        this.csvHeaders = [];
        this.csvData = [];
        this.paginatedData = [];
        return;
      }

      const filtered = this.filterColumns(rawHeaders, rawData);
      this.csvHeaders = filtered.headers;
      this.csvData = filtered.data;
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
    this.doImport();
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

  protected normalizeHeaderForComparison(header: string): string {
    return header.trim();
  }

  protected filterColumns(
    headers: string[],
    data: string[][]
  ): { headers: string[]; data: string[][] } {
    return {headers, data};
  }

  protected abstract doImport(): void;

  protected abstract loadAvailableCount(): void;
}

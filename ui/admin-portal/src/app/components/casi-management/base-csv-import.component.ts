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

import {Directive, OnInit} from '@angular/core';

@Directive()
export abstract class BaseCsvImportComponent implements OnInit {
  error: string | null = null;
  working = false;
  csvHeaders: string[] = [];
  csvData: string[][] = [];
  paginatedData: string[][] = [];
  selectedFile: File | null = null;
  csvImported = false;
  currentPage = 1;
  pageSize = 30;  // Number of items per page
  // List of required columns
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

      // Extract raw headers and data
      const rawHeaders = lines[0].split(',').map((header) => header.trim());
      const rawData = lines.slice(1).map((line) => line.split(','));
      const normalizedHeaders = rawHeaders.map((header) =>
        this.normalizeHeaderForComparison(header)
      );

      // Validate headers
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

  normalizeHeaderForComparison(header: string): string {
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

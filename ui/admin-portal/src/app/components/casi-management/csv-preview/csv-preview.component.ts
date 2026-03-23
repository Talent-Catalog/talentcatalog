/*
 * Copyright (c) 2024 Talent Catalog.
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

import {Component, EventEmitter, Input, Output} from '@angular/core';


/**
 * Component for previewing CSV data in a paginated table format.
 * Displays CSV headers and data, and allows users to navigate through pages of data.
 *
 * @author sadatmalik
 */
@Component({
  selector: 'app-csv-preview',
  templateUrl: './csv-preview.component.html'
})
export class CsvPreviewComponent {
  @Input() csvHeaders: string[] = [];
  @Input() csvData: string[][] = [];
  @Input() paginatedData: string[][] = [];
  @Input() working = false;
  @Input() currentPage = 1;
  @Input() pageSize = 30;

  @Output() pageChange = new EventEmitter<number>();

  onPageChanged(page: number): void {
    this.pageChange.emit(page);
  }
}

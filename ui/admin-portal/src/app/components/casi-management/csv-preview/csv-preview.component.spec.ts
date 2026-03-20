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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {CsvPreviewComponent} from './csv-preview.component';

describe('CsvPreviewComponent', () => {
  let component: CsvPreviewComponent;
  let fixture: ComponentFixture<CsvPreviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CsvPreviewComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CsvPreviewComponent);
    component = fixture.componentInstance;
    component.csvHeaders = ['A', 'B'];
    component.csvData = [['1', '2']];
    component.paginatedData = [['1', '2']];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('emits pageChange when page changes', () => {
    spyOn(component.pageChange, 'emit');
    component.onPageChanged(2);
    expect(component.pageChange.emit).toHaveBeenCalledWith(2);
  });
});

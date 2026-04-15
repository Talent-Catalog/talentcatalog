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

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of, throwError} from 'rxjs';
import {CasiAdminService} from "../../../services/casi-admin.service";
import {ImportReferenceVouchersComponent} from './import-reference-vouchers.component';

describe('ImportReferenceVouchersComponent', () => {
  let component: ImportReferenceVouchersComponent;
  let fixture: ComponentFixture<ImportReferenceVouchersComponent>;
  let mockCasiAdminService: jasmine.SpyObj<CasiAdminService>;

  beforeEach(async () => {
    mockCasiAdminService = jasmine.createSpyObj('CasiAdminService',
      ['importInventory', 'countAvailable']);

    await TestBed.configureTestingModule({
      declarations: [ImportReferenceVouchersComponent],
      providers: [{provide: CasiAdminService, useValue: mockCasiAdminService}],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ImportReferenceVouchersComponent);
    component = fixture.componentInstance;
    mockCasiAdminService.countAvailable.and.returnValue(of({count: 10}));
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should set an error if import is triggered without selecting a file', () => {
    component.selectedFile = null;

    component.importCSV();

    expect(component.error).toBe('Please select a file to import.');
  });

  it('should import csv via generic CasiAdminService', () => {
    component.selectedFile = new File(['voucher_code,expires_at\nREF-1,2026-12-01'], 'reference.csv');
    component.csvData = [['REF-1', '2026-12-01']];
    mockCasiAdminService.importInventory.and.returnValue(of({status: 'success'}));

    component.importCSV();

    expect(mockCasiAdminService.importInventory)
      .toHaveBeenCalledWith('REFERENCE', 'VOUCHER', component.selectedFile);
  });

  it('should set import error when service fails', () => {
    component.selectedFile = new File(['voucher_code,expires_at\nREF-1,2026-12-01'], 'reference.csv');
    component.csvData = [['REF-1', '2026-12-01']];
    mockCasiAdminService.importInventory.and.returnValue(throwError(() => new Error('boom')));

    component.importCSV();

    expect(component.error).toBe('Failed to import the CSV file. Please try again.');
  });
});

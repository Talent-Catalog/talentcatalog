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
import {LinkedinPremiumCouponService} from "../../../services/linkedin-premium-coupon.service";
import {ImportLinkedinPremiumCouponsComponent} from './import-linkedin-premium-coupons.component';

describe('ImportLinkedinPremiumCouponsComponent', () => {
  let component: ImportLinkedinPremiumCouponsComponent;
  let fixture: ComponentFixture<ImportLinkedinPremiumCouponsComponent>;
  let mockCouponService: jasmine.SpyObj<LinkedinPremiumCouponService>;

  beforeEach(async () => {
    mockCouponService = jasmine.createSpyObj('LinkedinPremiumCouponService', [
      'importCoupons',
      'countAvailableCoupons'
    ]);

    await TestBed.configureTestingModule({
      declarations: [ImportLinkedinPremiumCouponsComponent],
      providers: [{provide: LinkedinPremiumCouponService, useValue: mockCouponService}],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ImportLinkedinPremiumCouponsComponent);
    component = fixture.componentInstance;
    mockCouponService.countAvailableCoupons.and.returnValue(of({count: 100}));
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should set an error message on service failure', () => {
    component.selectedFile = new File([], 'coupons.csv');
    mockCouponService.importCoupons.and.returnValue(throwError(() => new Error('Error')));

    component.importCSV();

    expect(component.working).toBeFalse();
    expect(component.error).toBe('Failed to import the CSV file. Please try again.');
  });

  it('should set an error message if no file is selected', () => {
    component.selectedFile = null;

    component.importCSV();

    expect(component.error).toBe('Please select a file to import.');
  });
});

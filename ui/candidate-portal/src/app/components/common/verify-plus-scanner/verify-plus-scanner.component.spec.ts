/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';

import {VerifyPlusScannerComponent} from './verify-plus-scanner.component';

describe('VerifyPlusScannerComponent', () => {
  let component: VerifyPlusScannerComponent;
  let fixture: ComponentFixture<VerifyPlusScannerComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [VerifyPlusScannerComponent],
      schemas: [NO_ERRORS_SCHEMA]
    });

    fixture = TestBed.createComponent(VerifyPlusScannerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit scanned payload on successful scan', () => {
    spyOn(component.scanned, 'emit');

    component.onScanSuccess('payload-value');

    expect(component.scanned.emit).toHaveBeenCalledWith('payload-value');
  });

  it('should mark invalid scan on scan failure', () => {
    component.onScanFailure();

    expect(component.invalidScan).toBeTrue();
  });

  it('should start scanning and clear invalid scan state', () => {
    component.invalidScan = true;
    component.scanning = false;

    component.startScanning();

    expect(component.scanning).toBeTrue();
    expect(component.invalidScan).toBeFalse();
  });

  it('should update hasDevices when cameras are found or missing', () => {
    component.onCamerasFound([{label: 'Front Camera'} as MediaDeviceInfo]);
    expect(component.hasDevices).toBeTrue();

    component.onCamerasNotFound();
    expect(component.hasDevices).toBeFalse();
  });

  it('should emit scanner errors', () => {
    spyOn(component.scannerError, 'emit');
    const error = new Error('scanner-failed');

    component.onScanError(error);

    expect(component.scannerError.emit).toHaveBeenCalledWith(error);
  });

  it('should stop scanning when permission is denied', () => {
    component.scanning = true;

    component.onPermissionResponse(false);

    expect(component.cameraPermission).toBeFalse();
    expect(component.scanning).toBeFalse();
  });
});

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
import {NO_ERRORS_SCHEMA, Pipe, PipeTransform} from '@angular/core';
import {of, throwError} from 'rxjs';

import {RegistrationVerifyPlusComponent} from './registration-verify-plus.component';
import {VerifyPlusService} from '../../../services/verify-plus.service';
import {RegistrationService} from '../../../services/registration.service';
import {AuthenticationService} from '../../../services/authentication.service';

@Pipe({name: 'translate'})
class MockTranslatePipe implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

describe('RegistrationVerifyPlusComponent', () => {
  let component: RegistrationVerifyPlusComponent;
  let fixture: ComponentFixture<RegistrationVerifyPlusComponent>;
  let verifyPlusService: jasmine.SpyObj<VerifyPlusService>;
  let registrationService: jasmine.SpyObj<RegistrationService>;
  let authenticationService: jasmine.SpyObj<AuthenticationService>;

  beforeEach(() => {
    verifyPlusService = jasmine.createSpyObj<VerifyPlusService>('VerifyPlusService', ['submitScan']);
    registrationService = jasmine.createSpyObj<RegistrationService>('RegistrationService', ['next', 'back']);
    authenticationService = jasmine.createSpyObj<AuthenticationService>('AuthenticationService', ['isGrnInstance']);
    authenticationService.isGrnInstance.and.returnValue(true);

    TestBed.configureTestingModule({
      declarations: [RegistrationVerifyPlusComponent, MockTranslatePipe],
      providers: [
        {provide: VerifyPlusService, useValue: verifyPlusService},
        {provide: RegistrationService, useValue: registrationService},
        {provide: AuthenticationService, useValue: authenticationService}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    });

    fixture = TestBed.createComponent(RegistrationVerifyPlusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should auto-skip step for non-GRN instances', () => {
    authenticationService.isGrnInstance.and.returnValue(false);

    const skippedFixture = TestBed.createComponent(RegistrationVerifyPlusComponent);
    skippedFixture.detectChanges();

    expect(registrationService.next).toHaveBeenCalled();
  });

  it('should return empty formattedPayload when nothing has been scanned', () => {
    component.decodedPayload = null;

    expect(component.formattedPayload).toBe('');
  });

  it('should pretty-print valid JSON in formattedPayload', () => {
    component.decodedPayload = '{"v":"mock-1","unhcrId":"123-45C67890"}';

    expect(component.formattedPayload).toBe(JSON.stringify({
      v: 'mock-1',
      unhcrId: '123-45C67890'
    }, null, 2));
  });

  it('should return raw payload when formattedPayload cannot parse JSON', () => {
    component.decodedPayload = 'not-json';

    expect(component.formattedPayload).toBe('not-json');
  });

  it('should disable next while submitting', () => {
    component.submitting = false;
    expect(component.nextDisabled).toBeFalse();

    component.submitting = true;
    expect(component.nextDisabled).toBeTrue();
  });

  it('should store decoded payload and clear previous submit state when scanner emits', () => {
    component.scannerError = new Error('previous');
    component.submitResult = {unhcrNumber: 'old', duplicate: false};
    component.submitError = true;
    component.submitErrorMessage = 'old error';

    component.onScanned('decoded-qr');

    expect(component.decodedPayload).toBe('decoded-qr');
    expect(component.scannerError).toBeNull();
    expect(component.submitResult).toBeNull();
    expect(component.submitError).toBeFalse();
    expect(component.submitErrorMessage).toBeNull();
  });

  it('should store scannerError when scanner fails', () => {
    const error = new Error('camera denied');

    component.onScannerError(error);

    expect(component.scannerError).toBe(error);
  });

  it('should not submit when there is no decoded payload', () => {
    component.decodedPayload = null;

    component.onConfirm();

    expect(verifyPlusService.submitScan).not.toHaveBeenCalled();
  });

  it('should not submit when already submitting', () => {
    component.decodedPayload = '{"v":"mock-1","unhcrId":"123-45C67890"}';
    component.submitting = true;

    component.onConfirm();

    expect(verifyPlusService.submitScan).not.toHaveBeenCalled();
  });

  it('should submit scanned payload and store result on confirm success', () => {
    const payload = '{"v":"mock-1","unhcrId":"123-45C67890"}';
    component.onScanned(payload);
    verifyPlusService.submitScan.and.returnValue(of({
      unhcrNumber: '123-45C67890',
      duplicate: false
    }));

    component.onConfirm();

    expect(verifyPlusService.submitScan).toHaveBeenCalledWith(payload);
    expect(component.submitResult).toEqual({
      unhcrNumber: '123-45C67890',
      duplicate: false
    });
    expect(component.submitError).toBeFalse();
    expect(component.submitting).toBeFalse();
  });

  it('should capture duplicate result on confirm success', () => {
    const payload = '{"v":"mock-1","unhcrId":"999-00A11111"}';
    component.onScanned(payload);
    verifyPlusService.submitScan.and.returnValue(of({
      unhcrNumber: '999-00A11111',
      duplicate: true
    }));

    component.onConfirm();

    expect(component.submitResult?.duplicate).toBeTrue();
  });

  it('should set submitError and capture message when confirm fails', () => {
    const payload = '{"v":"mock-2","unhcrId":"123-45C67890"}';
    const errorMessage = 'Unsupported Verify+ payload version: mock-2';
    component.onScanned(payload);
    verifyPlusService.submitScan.and.returnValue(throwError(errorMessage));

    component.onConfirm();

    expect(verifyPlusService.submitScan).toHaveBeenCalledWith(payload);
    expect(component.submitError).toBeTrue();
    expect(component.submitErrorMessage).toBe(errorMessage);
  });

  it('should set submitError without message when confirm fails with a non-string error', () => {
    const payload = '{"v":"mock-2","unhcrId":"123-45C67890"}';
    component.onScanned(payload);
    verifyPlusService.submitScan.and.returnValue(throwError({status: 500}));

    component.onConfirm();

    expect(component.submitError).toBeTrue();
    expect(component.submitErrorMessage).toBeNull();
  });

  it('should reset submit state and restart scanner when rescanning', () => {
    const startScanning = jasmine.createSpy('startScanning');
    component.scanner = {startScanning} as any;
    component.decodedPayload = '{"v":"mock-1","unhcrId":"123-45C67890"}';
    component.submitResult = {unhcrNumber: '123-45C67890', duplicate: true};
    component.submitError = true;
    component.submitErrorMessage = 'Unsupported Verify+ payload version: mock-2';
    component.scannerError = new Error('scanner');

    component.onRescan();

    expect(component.decodedPayload).toBeNull();
    expect(component.submitResult).toBeNull();
    expect(component.submitError).toBeFalse();
    expect(component.submitErrorMessage).toBeNull();
    expect(startScanning).toHaveBeenCalled();
  });

  it('should move to next step when continuing or skipping', () => {
    component.onNext();

    expect(registrationService.next).toHaveBeenCalled();
  });

  it('should go back to previous step', () => {
    component.onBack();

    expect(registrationService.back).toHaveBeenCalled();
  });
});

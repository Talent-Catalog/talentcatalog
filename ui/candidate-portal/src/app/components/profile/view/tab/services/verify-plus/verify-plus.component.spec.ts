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

import {VerifyPlusComponent} from './verify-plus.component';
import {VerifyPlusService} from '../../../../../../services/verify-plus.service';

@Pipe({name: 'translate'})
class MockTranslatePipe implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

describe('VerifyPlusComponent', () => {
  let component: VerifyPlusComponent;
  let fixture: ComponentFixture<VerifyPlusComponent>;
  let verifyPlusService: jasmine.SpyObj<VerifyPlusService>;

  beforeEach(() => {
    verifyPlusService = jasmine.createSpyObj<VerifyPlusService>('VerifyPlusService', ['submitScan']);

    TestBed.configureTestingModule({
      declarations: [VerifyPlusComponent, MockTranslatePipe],
      providers: [
        {provide: VerifyPlusService, useValue: verifyPlusService}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    });

    fixture = TestBed.createComponent(VerifyPlusComponent);
    component = fixture.componentInstance;
    component.candidate = {id: 1} as any;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should store decoded payload when scanner emits', () => {
    component.consentGiven = true;
    component.onScanned('decoded-qr');

    expect(component.decodedPayload).toBe('decoded-qr');
    expect(component.consentGiven).toBeFalse();
    expect(component.scannerError).toBeNull();
  });

  it('should emit backButtonClicked when back clicked', () => {
    spyOn(component.backButtonClicked, 'emit');

    component.onBackButtonClicked();

    expect(component.backButtonClicked.emit).toHaveBeenCalled();
  });

  it('should not submit scanned payload when consent has not been given', () => {
    component.onScanned('{"v":"mock-1","unhcrId":"123-45C67890"}');

    component.onConfirm();

    expect(verifyPlusService.submitScan).not.toHaveBeenCalled();
  });

  it('should submit scanned payload and store result on confirm success', () => {
    const payload = '{"v":"mock-1","unhcrId":"123-45C67890"}';
    component.onScanned(payload);
    component.consentGiven = true;
    verifyPlusService.submitScan.and.returnValue(of({
      unhcrNumber: '123-45C67890',
      duplicate: false
    }));

    component.onConfirm();

    expect(verifyPlusService.submitScan).toHaveBeenCalledWith(payload, true);
    expect(component.submitResult).toEqual({
      unhcrNumber: '123-45C67890',
      duplicate: false
    });
    expect(component.submitError).toBeFalse();
    expect(component.submitting).toBeFalse();
  });

  it('should set submitError and capture message when confirm fails', () => {
    const payload = '{"v":"mock-2","unhcrId":"123-45C67890"}';
    const errorMessage = 'Unsupported Verify+ payload version: mock-2';

    component.onScanned(payload);
    component.consentGiven = true;

    verifyPlusService.submitScan.and.returnValue(
      throwError(errorMessage)
    );

    component.onConfirm();

    expect(verifyPlusService.submitScan).toHaveBeenCalledWith(payload, true);
    expect(component.submitError).toBeTrue();
    expect(component.submitErrorMessage).toBe(errorMessage);
    expect(component.submitting).toBeFalse();
  });

  it('should reset submit state when rescanning', () => {
    component.decodedPayload = '{"v":"mock-1","unhcrId":"123-45C67890"}';
    component.consentGiven = true;
    component.submitResult = {unhcrNumber: '123-45C67890', duplicate: true};
    component.submitError = true;
    component.submitErrorMessage = 'Unsupported Verify+ payload version: mock-2';
    component.scannerError = new Error('scanner');

    component.onRescan();

    expect(component.decodedPayload).toBeNull();
    expect(component.consentGiven).toBeFalse();
    expect(component.submitResult).toBeNull();
    expect(component.submitError).toBeFalse();
    expect(component.submitErrorMessage).toBeNull();
  });
});

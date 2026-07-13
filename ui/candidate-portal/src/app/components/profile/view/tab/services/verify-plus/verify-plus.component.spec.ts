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

import {VerifyPlusComponent} from './verify-plus.component';

describe('VerifyPlusComponent', () => {
  let component: VerifyPlusComponent;
  let fixture: ComponentFixture<VerifyPlusComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [VerifyPlusComponent],
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
    component.onScanned('decoded-qr');

    expect(component.decodedPayload).toBe('decoded-qr');
    expect(component.scannerError).toBeNull();
  });

  it('should emit backButtonClicked when back clicked', () => {
    spyOn(component.backButtonClicked, 'emit');

    component.onBackButtonClicked();

    expect(component.backButtonClicked.emit).toHaveBeenCalled();
  });
});

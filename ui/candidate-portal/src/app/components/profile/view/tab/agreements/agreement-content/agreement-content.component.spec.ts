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
import {By} from '@angular/platform-browser';
import {AgreementContentComponent} from './agreement-content.component';

describe('AgreementContentComponent', () => {
  let component: AgreementContentComponent;
  let fixture: ComponentFixture<AgreementContentComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AgreementContentComponent],
      schemas: [NO_ERRORS_SCHEMA]
    });

    fixture = TestBed.createComponent(AgreementContentComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render input values', () => {
    component.content = '<h1>Test Agreement</h1>';
    component.counterpartyName = 'OPC';
    component.acceptedId = 'GrnCandidatePrivacyPolicyV2';
    component.acceptedDate = '2026-05-20';

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('GrnCandidatePrivacyPolicyV2');
    expect(fixture.nativeElement.textContent).toContain('May 20, 2026');
  });

  it('should emit back when back button is clicked', () => {
    const backSpy = spyOn(component.back, 'emit');
    component.back.subscribe(() => {});
    fixture.detectChanges();

    const button = fixture.debugElement.query(By.css('tc-button'));
    button.triggerEventHandler('onClick', null);

    expect(backSpy).toHaveBeenCalled();
  });
});

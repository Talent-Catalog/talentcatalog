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
import {of} from 'rxjs';
import {AgreementService} from '../../../../../services/agreement.service';
import {CandidateAgreementsComponent} from './candidate-agreements.component';

describe('CandidateAgreementsComponent', () => {
  let component: CandidateAgreementsComponent;
  let fixture: ComponentFixture<CandidateAgreementsComponent>;
  let agreementServiceSpy: jasmine.SpyObj<AgreementService>;

  beforeEach(() => {
    agreementServiceSpy = jasmine.createSpyObj('AgreementService', ['listMyAgreements']);
    agreementServiceSpy.listMyAgreements.and.returnValue(of([]));

    TestBed.configureTestingModule({
      declarations: [CandidateAgreementsComponent],
      providers: [{provide: AgreementService, useValue: agreementServiceSpy}],
      schemas: [NO_ERRORS_SCHEMA]
    });

    fixture = TestBed.createComponent(CandidateAgreementsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load agreements on init', () => {
    expect(agreementServiceSpy.listMyAgreements).toHaveBeenCalled();
    expect(component.agreements.length).toBe(0);
  });

  it('should return true for active agreement', () => {
    expect(component.isActive({
      id: 1,
      start: '2026-01-01T00:00:00Z',
      end: null,
      termsInfoId: 'TestTermsV1',
      counterparty: {id: 1, type: 'DATABASE_PROVIDER', name: 'OPC'},
      termsInfo: {id: 'TestTermsV1', type: 'GRN_CANDIDATE_PRIVACY_POLICY', pathToContent: '', createdDate: '2026-01-01', content: ''}
    })).toBeTrue();
  });
});

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

  it('should return true for active agreement with null end', () => {
    expect(component.isActive({
      id: 1,
      start: '2026-01-01T00:00:00Z',
      end: null,
      termsInfoId: 'TestTermsV1',
      counterparty: {id: 1, type: 'DATABASE_PROVIDER', displayName: 'OPC'},
      termsInfo: {id: 'TestTermsV1', type: 'GRN_CANDIDATE_PRIVACY_POLICY', pathToContent: '', createdDate: '2026-01-01', content: ''}
    })).toBeTrue();
  });

  it('should return true for active agreement with undefined end (DtoBuilder omits null fields)', () => {
    const agreementWithoutEnd: any = {
      id: 2,
      start: '2026-01-01T00:00:00Z',
      termsInfoId: 'TestTermsV1',
      counterparty: {id: 1, type: 'DATABASE_PROVIDER', displayName: 'OPC'},
      termsInfo: {id: 'TestTermsV1', type: 'GRN_CANDIDATE_PRIVACY_POLICY', pathToContent: '', createdDate: '2026-01-01', content: ''}
    };
    expect(component.isActive(agreementWithoutEnd)).toBeTrue();
  });

  it('should return false for superseded agreement with end date set', () => {
    expect(component.isActive({
      id: 3,
      start: '2026-01-01T00:00:00Z',
      end: '2026-06-01T00:00:00Z',
      termsInfoId: 'TestTermsV1',
      counterparty: {id: 1, type: 'DATABASE_PROVIDER', displayName: 'OPC'},
      termsInfo: {id: 'TestTermsV1', type: 'GRN_CANDIDATE_PRIVACY_POLICY', pathToContent: '', createdDate: '2026-01-01', content: ''}
    })).toBeFalse();
  });

  it('should set selectedAgreement when agreement is clicked', () => {
    const agreement = {
      id: 1,
      start: '2026-01-01T00:00:00Z',
      end: null,
      termsInfoId: 'TestTermsV1',
      counterparty: {id: 1, type: 'DATABASE_PROVIDER', displayName: 'OPC'},
      termsInfo: {id: 'TestTermsV1', type: 'GRN_CANDIDATE_PRIVACY_POLICY', pathToContent: '', createdDate: '2026-01-01', content: ''}
    };

    component.viewAgreement(agreement);

    expect(component.selectedAgreement).toEqual(agreement);
  });

  it('should clear selected agreement', () => {
    component.selectedAgreement = {
      id: 1,
      start: '2026-01-01T00:00:00Z',
      end: null,
      termsInfoId: 'TestTermsV1',
      counterparty: {id: 1, type: 'DATABASE_PROVIDER', displayName: 'OPC'},
      termsInfo: {id: 'TestTermsV1', type: 'GRN_CANDIDATE_PRIVACY_POLICY', pathToContent: '', createdDate: '2026-01-01', content: ''}
    };

    component.clearSelection();

    expect(component.selectedAgreement).toBeNull();
  });
});

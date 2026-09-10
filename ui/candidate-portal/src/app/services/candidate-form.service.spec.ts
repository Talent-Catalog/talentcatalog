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
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';

import {environment} from '../../environments/environment';
import {
  DependantsTravelInfoFormComponent
} from '../components/form/family-doc-form/dependants-travel-info-form.component';
import {
  DependantsRefugeeStatusInfoFormComponent
} from '../components/form/family-rsd-evidence-form/dependants-refugee-status-info-form.component';
import {
  TravelInfoFormComponent
} from '../components/form/italy-travel-document-form/travel-info-form.component';
import {MyFirstFormComponent} from '../components/form/my-first-form/my-first-form.component';
import {
  RefugeeStatusInfoFormComponent
} from '../components/form/rsd-evidence-form/refugee-status-info-form.component';
import {
  DependantsInfoFormData,
  MyFirstFormData,
  RefugeeStatusInfoFormData,
  TravelDocType,
  TravelInfoFormData
} from '../model/form';
import {CandidateFormService} from './candidate-form.service';

describe('CandidateFormService', () => {
  let service: CandidateFormService;
  let httpMock: HttpTestingController;

  const apiUrl = `${environment.apiUrl}/form`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });

    service = TestBed.inject(CandidateFormService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
    expect(service.apiUrl).toBe(apiUrl);
  });

  describe('MyFirstForm', () => {
    const formData: MyFirstFormData = {
      city: 'Kabul',
      hairColour: 'Black'
    };

    it('should create or update the form', () => {
      service.createOrUpdateMyFirstForm(formData).subscribe((response) => {
        expect(response).toEqual(formData);
      });

      const request = httpMock.expectOne(`${apiUrl}/my-first-form`);

      expect(request.request.method).toBe('POST');
      expect(request.request.body).toEqual(formData);

      request.flush(formData);
    });

    it('should fetch the form', () => {
      service.getMyFirstForm().subscribe((response) => {
        expect(response).toEqual(formData);
      });

      const request = httpMock.expectOne(`${apiUrl}/my-first-form`);

      expect(request.request.method).toBe('GET');

      request.flush(formData);
    });
  });

  describe('TravelInfoForm', () => {
    const formData: TravelInfoFormData = {
      firstName: 'Ehsan',
      lastName: 'Ehrari',
      dateOfBirth: '1995-07-27',
      gender: 'Male',
      birthCountry: null,
      placeOfBirth: 'Kabul',
      travelDocType: TravelDocType.Passport,
      travelDocNumber: 'P123456',
      travelDocIssuedBy: 'Afghanistan',
      travelDocIssueDate: '2025-01-01',
      travelDocExpiryDate: '2030-01-01',
      travelInfoComment: 'Test comment'
    };

    it('should create or update the form', () => {
      service.createOrUpdateTravelInfoForm(formData).subscribe((response) => {
        expect(response).toEqual(formData);
      });

      const request = httpMock.expectOne(`${apiUrl}/travel-info-form`);

      expect(request.request.method).toBe('POST');
      expect(request.request.body).toEqual(formData);

      request.flush(formData);
    });

    it('should fetch the form', () => {
      service.getTravelInfoForm().subscribe((response) => {
        expect(response).toEqual(formData);
      });

      const request = httpMock.expectOne(`${apiUrl}/travel-info-form`);

      expect(request.request.method).toBe('GET');

      request.flush(formData);
    });
  });

  describe('DependantsInfoForm', () => {
    const formData: DependantsInfoFormData = {
      dependantsInfoJson: JSON.stringify([
        {
          'user.firstName': 'Test',
          'user.lastName': 'Dependant'
        }
      ]),
      noEligibleDependants: false,
      noEligibleNotes: ''
    };

    it('should create or update the form', () => {
      service.createOrUpdateDependantsInfoForm(formData)
      .subscribe((response) => {
        expect(response).toEqual(formData);
      });

      const request = httpMock.expectOne(
        `${apiUrl}/dependants-info-form`
      );

      expect(request.request.method).toBe('POST');
      expect(request.request.body).toEqual(formData);

      request.flush(formData);
    });

    it('should fetch the form', () => {
      service.getDependantsInfoForm().subscribe((response) => {
        expect(response).toEqual(formData);
      });

      const request = httpMock.expectOne(
        `${apiUrl}/dependants-info-form`
      );

      expect(request.request.method).toBe('GET');

      request.flush(formData);
    });
  });

  describe('RefugeeStatusInfoForm', () => {
    const formData: RefugeeStatusInfoFormData = {
      refugeeStatus: null,
      documentType: null,
      documentNumber: 'DOC-123',
      refugeeStatusComment: 'Pending verification'
    };

    it('should create or update the form', () => {
      service.createOrUpdateRefugeeStatusInfoForm(formData)
      .subscribe((response) => {
        expect(response).toEqual(formData);
      });

      const request = httpMock.expectOne(
        `${apiUrl}/refugee-status-info-form`
      );

      expect(request.request.method).toBe('POST');
      expect(request.request.body).toEqual(formData);

      request.flush(formData);
    });

    it('should fetch the form', () => {
      service.getRefugeeStatusInfoForm().subscribe((response) => {
        expect(response).toEqual(formData);
      });

      const request = httpMock.expectOne(
        `${apiUrl}/refugee-status-info-form`
      );

      expect(request.request.method).toBe('GET');

      request.flush(formData);
    });
  });

  describe('getFormComponentByName', () => {
    it('should return each registered form component', () => {
      const mappings = [
        ['MyFirstForm', MyFirstFormComponent],
        ['TravelInfoForm', TravelInfoFormComponent],
        ['RefugeeStatusInfoForm', RefugeeStatusInfoFormComponent],
        ['DependantsTravelInfoForm', DependantsTravelInfoFormComponent],
        [
          'DependantsRefugeeStatusInfoForm',
          DependantsRefugeeStatusInfoFormComponent
        ]
      ];

      mappings.forEach(([formName, expectedComponent]) => {
        expect(service.getFormComponentByName(formName as string))
        .toBe(expectedComponent);
      });
    });

    it('should return undefined for an unknown form name', () => {
      expect(service.getFormComponentByName('UnknownForm'))
      .toBeUndefined();
    });
  });
});

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

import {FormBuilder} from '@angular/forms';
import {of, Subject} from 'rxjs';

import {
  RefugeeStatusEvidenceDocumentType,
  RefugeeStatusInfoFormData,
  RsdRefugeeStatus
} from '../../../model/form';
import {CandidateFormService} from '../../../services/candidate-form.service';
import {RefugeeStatusInfoFormComponent} from './refugee-status-info-form.component';

describe('RefugeeStatusInfoFormComponent', () => {
  let component: RefugeeStatusInfoFormComponent;
  let candidateFormService:
    jasmine.SpyObj<CandidateFormService>;

  beforeEach(() => {
    candidateFormService =
      jasmine.createSpyObj<CandidateFormService>(
        'CandidateFormService',
        [
          'getRefugeeStatusInfoForm',
          'createOrUpdateRefugeeStatusInfoForm'
        ]
      );

    component =
      new RefugeeStatusInfoFormComponent(
        candidateFormService,
        new FormBuilder()
      );
  });

  it('should create with the initial state', () => {
    expect(component).toBeTruthy();
    expect(component.form).toBeUndefined();
    expect(component.error).toBeNull();
    expect(component.submitting).toBeFalse();
    expect(component.readOnly).toBeFalse();
  });

  it('should expose the refugee-status options', () => {
    expect(component.refugeeStatusOptions).toEqual([
      {
        value: RsdRefugeeStatus.RecognizedByUnhcr,
        label: 'Recognized by UNHCR'
      },
      {
        value: RsdRefugeeStatus.RecognizedByHostCountry,
        label: 'Recognized by host country'
      },
      {
        value: RsdRefugeeStatus.Pending,
        label: 'Pending'
      }
    ]);
  });

  it('should expose the document-type options', () => {
    expect(component.documentTypeOptions).toEqual([
      {
        value:
        RefugeeStatusEvidenceDocumentType
          .UnhcrCertificate,
        label: 'UNHCR Certificate of Recognition'
      },
      {
        value:
        RefugeeStatusEvidenceDocumentType
          .HostCountryId,
        label:
          'Host Country Refugee ID or Residence Permit'
      },
      {
        value:
        RefugeeStatusEvidenceDocumentType
          .OfficialCampRegistration,
        label:
          'Registration Document from Official Refugee Camp'
      }
    ]);
  });

  it('should initialise the form controls', () => {
    initialiseEmptyForm();

    expect(component.form).toBeTruthy();

    expect(
      component.form.contains('refugeeStatus')
    ).toBeTrue();

    expect(
      component.form.contains('documentType')
    ).toBeTrue();

    expect(
      component.form.contains('documentNumber')
    ).toBeTrue();

    expect(
      component.form.contains('refugeeStatusComment')
    ).toBeTrue();

    expect(
      candidateFormService.getRefugeeStatusInfoForm
    ).toHaveBeenCalledTimes(1);
  });

  it('should load previously saved form data', () => {
    const savedData = createFormData();

    initialiseForm(savedData);

    expect(
      component.form.get('refugeeStatus')?.value
    ).toBe(savedData.refugeeStatus);

    expect(
      component.form.get('documentType')?.value
    ).toBe(savedData.documentType);

    expect(
      component.form.get('documentNumber')?.value
    ).toBe('DOC-123');

    expect(
      component.form
      .get('refugeeStatusComment')
        ?.value
    ).toBe('Existing comment');
  });

  it('should clear an existing error during initialisation', () => {
    component.error = new Error(
      'Previous error'
    );

    initialiseForm();

    expect(component.error).toBeNull();
  });

  it('should reset the form when loading data fails', () => {
    const response$ =
      new Subject<RefugeeStatusInfoFormData>();

    candidateFormService
    .getRefugeeStatusInfoForm
    .and.returnValue(response$);

    component.ngOnInit();

    component.form.patchValue({
      refugeeStatus:
      RsdRefugeeStatus.RecognizedByUnhcr,
      documentType:
      RefugeeStatusEvidenceDocumentType
        .UnhcrCertificate,
      documentNumber: 'TEMP-123',
      refugeeStatusComment: 'Temporary comment'
    });

    response$.error(
      new Error('Form does not exist')
    );

    expect(
      component.form.get('refugeeStatus')?.value
    ).toBeNull();

    expect(
      component.form.get('documentType')?.value
    ).toBeNull();

    expect(
      component.form.get('documentNumber')?.value
    ).toBeNull();

    expect(
      component.form
      .get('refugeeStatusComment')
        ?.value
    ).toBeNull();

    expect(component.form.invalid).toBeTrue();
  });
  it('should disable the form in read-only mode', () => {
    component.readOnly = true;

    initialiseForm();

    expect(component.form.disabled).toBeTrue();

    Object.values(component.form.controls)
    .forEach(control => {
      expect(control.disabled).toBeTrue();
    });
  });

  it('should leave the form enabled in editable mode', () => {
    component.readOnly = false;

    initialiseForm();

    expect(component.form.enabled).toBeTrue();

    Object.values(component.form.controls)
    .forEach(control => {
      expect(control.enabled).toBeTrue();
    });
  });

  describe('validation', () => {
    it('should require refugee status', () => {
      initialiseEmptyForm();

      expect(
        component.form
        .get('refugeeStatus')
        ?.hasError('required')
      ).toBeTrue();
    });

    it('should require document type', () => {
      initialiseEmptyForm();

      expect(
        component.form
        .get('documentType')
        ?.hasError('required')
      ).toBeTrue();
    });

    it('should allow an empty document number', () => {
      initialiseEmptyForm();

      expect(
        component.form
        .get('documentNumber')
          ?.valid
      ).toBeTrue();
    });

    it('should allow a 30-character document number', () => {
      initialiseEmptyForm();

      component.form
      .get('documentNumber')
      ?.setValue('1'.repeat(30));

      expect(
        component.form
        .get('documentNumber')
          ?.valid
      ).toBeTrue();
    });

    it('should reject a document number longer than 30 characters', () => {
      initialiseEmptyForm();

      component.form
      .get('documentNumber')
      ?.setValue('1'.repeat(31));

      expect(
        component.form
        .get('documentNumber')
        ?.hasError('maxlength')
      ).toBeTrue();
    });

    it('should be valid when required fields are supplied', () => {
      initialiseEmptyForm();

      setValidFormValues();

      expect(component.form.valid).toBeTrue();
    });
  });

  describe('canSubmit', () => {
    it('should return true for a valid editable form', () => {
      initialiseEmptyForm();
      setValidFormValues();

      expect(component.canSubmit()).toBeTrue();
    });

    it('should return false for an invalid form', () => {
      initialiseEmptyForm();

      expect(component.form.invalid).toBeTrue();
      expect(component.canSubmit()).toBeFalse();
    });

    it('should return false while validation is pending', () => {
      initialiseEmptyForm();
      setValidFormValues();

      component.form.markAsPending();

      expect(component.canSubmit()).toBeFalse();
    });

    it('should return false while submitting', () => {
      initialiseEmptyForm();
      setValidFormValues();

      component.submitting = true;

      expect(component.canSubmit()).toBeFalse();
    });

    it('should return false in read-only mode', () => {
      component.readOnly = true;

      initialiseForm();

      expect(component.canSubmit()).toBeFalse();
    });
  });

  describe('hasError', () => {
    it('should return false for an untouched invalid control', () => {
      initialiseEmptyForm();

      expect(
        component.hasError(
          'refugeeStatus',
          'required'
        )
      ).toBeFalse();
    });

    it('should return true for a touched required control', () => {
      initialiseEmptyForm();

      const control =
        component.form.get('refugeeStatus');

      control?.markAsTouched();

      expect(control?.hasError('required'))
      .toBeTrue();

      expect(
        component.hasError(
          'refugeeStatus',
          'required'
        )
      ).toBeTrue();
    });

    it('should detect the maximum-length error', () => {
      initialiseEmptyForm();

      const control =
        component.form.get('documentNumber');

      control?.setValue('1'.repeat(31));
      control?.markAsTouched();

      expect(
        component.hasError(
          'documentNumber',
          'maxlength'
        )
      ).toBeTrue();
    });

    it('should return false for a valid control', () => {
      initialiseEmptyForm();

      const control =
        component.form.get('refugeeStatus');

      control?.setValue(
        RsdRefugeeStatus.RecognizedByUnhcr
      );

      control?.markAsTouched();

      expect(
        component.hasError(
          'refugeeStatus',
          'required'
        )
      ).toBeFalse();
    });

    it('should return false in read-only mode', () => {
      component.readOnly = true;

      initialiseEmptyForm();

      component.form
      .get('refugeeStatus')
      ?.markAsTouched();

      expect(
        component.hasError(
          'refugeeStatus',
          'required'
        )
      ).toBeFalse();
    });

    it('should return false for an unknown control', () => {
      initialiseEmptyForm();

      expect(
        component.hasError(
          'unknown' as keyof RefugeeStatusInfoFormData,
          'required'
        )
      ).toBeFalse();
    });
  });

  describe('onSubmit', () => {
    it('should mark all controls touched when invalid', () => {
      initialiseEmptyForm();

      expect(component.form.invalid).toBeTrue();

      component.onSubmit();

      expect(
        component.form
        .get('refugeeStatus')
          ?.touched
      ).toBeTrue();

      expect(
        component.form
        .get('documentType')
          ?.touched
      ).toBeTrue();

      expect(
        component.form
        .get('documentNumber')
          ?.touched
      ).toBeTrue();

      expect(
        component.form
        .get('refugeeStatusComment')
          ?.touched
      ).toBeTrue();

      expect(
        candidateFormService
          .createOrUpdateRefugeeStatusInfoForm
      ).not.toHaveBeenCalled();
    });

    it('should submit raw form data', () => {
      const response$ =
        new Subject<RefugeeStatusInfoFormData>();

      initialiseEmptyForm();
      setValidFormValues();

      candidateFormService
      .createOrUpdateRefugeeStatusInfoForm
      .and.returnValue(response$);

      component.error = new Error(
        'Previous error'
      );

      component.onSubmit();

      expect(component.submitting).toBeTrue();
      expect(component.error).toBeNull();

      expect(
        candidateFormService
          .createOrUpdateRefugeeStatusInfoForm
      ).toHaveBeenCalledTimes(1);

      const request =
        candidateFormService
        .createOrUpdateRefugeeStatusInfoForm
        .calls.mostRecent().args[0];

      expect(request).toEqual(
        createFormData()
      );
    });

    it('should emit saved data after successful submission', () => {
      const response$ =
        new Subject<RefugeeStatusInfoFormData>();

      const savedData = createFormData();

      initialiseEmptyForm();
      setValidFormValues();

      candidateFormService
      .createOrUpdateRefugeeStatusInfoForm
      .and.returnValue(response$);

      const submittedSpy = spyOn(
        component.submitted,
        'emit'
      );

      component.onSubmit();

      response$.next(savedData);

      expect(submittedSpy)
      .toHaveBeenCalledOnceWith(savedData);

      expect(component.submitting).toBeFalse();
    });

    it('should mark the form pristine and untouched after success', () => {
      const response$ =
        new Subject<RefugeeStatusInfoFormData>();

      initialiseEmptyForm();
      setValidFormValues();

      candidateFormService
      .createOrUpdateRefugeeStatusInfoForm
      .and.returnValue(response$);

      component.form.markAsDirty();
      component.form.markAllAsTouched();

      expect(component.form.dirty).toBeTrue();
      expect(component.form.touched).toBeTrue();

      component.onSubmit();

      response$.next(createFormData());

      expect(component.form.pristine).toBeTrue();
      expect(component.form.untouched).toBeTrue();
      expect(component.submitting).toBeFalse();
    });

    it('should store submission errors', () => {
      const response$ =
        new Subject<RefugeeStatusInfoFormData>();

      const error = new Error(
        'Unable to save refugee information'
      );

      initialiseEmptyForm();
      setValidFormValues();

      candidateFormService
      .createOrUpdateRefugeeStatusInfoForm
      .and.returnValue(response$);

      component.onSubmit();

      expect(component.submitting).toBeTrue();

      response$.error(error);

      expect(component.error).toBe(error);
      expect(component.submitting).toBeFalse();
    });
  });

  function initialiseForm(
    data: RefugeeStatusInfoFormData =
      createFormData()
  ): void {
    candidateFormService
    .getRefugeeStatusInfoForm
    .and.returnValue(of(data));

    component.ngOnInit();
  }

  function initialiseEmptyForm(): void {
    candidateFormService
    .getRefugeeStatusInfoForm
    .and.returnValue(
      of({} as RefugeeStatusInfoFormData)
    );

    component.ngOnInit();
  }

  function setValidFormValues(): void {
    component.form.setValue(
      createFormData()
    );

    component.form.updateValueAndValidity();
  }
});

function createFormData():
  RefugeeStatusInfoFormData {
  return {
    refugeeStatus:
    RsdRefugeeStatus.RecognizedByUnhcr,
    documentType:
    RefugeeStatusEvidenceDocumentType
      .UnhcrCertificate,
    documentNumber: 'DOC-123',
    refugeeStatusComment: 'Existing comment'
  } as RefugeeStatusInfoFormData;
}

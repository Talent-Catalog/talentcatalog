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
import {Router} from '@angular/router';
import {of, Subject} from 'rxjs';

import {Country} from '../../../model/country';
import {TravelDocType, TravelInfoFormData} from '../../../model/form';
import {CandidateFormService} from '../../../services/candidate-form.service';
import {CountryService} from '../../../services/country.service';
import {TravelInfoFormComponent} from './travel-info-form.component';

describe('TravelInfoFormComponent', () => {
  let component: TravelInfoFormComponent;
  let candidateFormService:
    jasmine.SpyObj<CandidateFormService>;
  let countryService:
    jasmine.SpyObj<CountryService>;
  let router: jasmine.SpyObj<Router>;

  const afghanistan = {
    id: 1,
    name: 'Afghanistan'
  } as Country;

  const italy = {
    id: 2,
    name: 'Italy'
  } as Country;

  beforeEach(() => {
    candidateFormService =
      jasmine.createSpyObj<CandidateFormService>(
        'CandidateFormService',
        [
          'getTravelInfoForm',
          'createOrUpdateTravelInfoForm'
        ]
      );

    countryService =
      jasmine.createSpyObj<CountryService>(
        'CountryService',
        ['listCountries']
      );

    router = jasmine.createSpyObj<Router>(
      'Router',
      [
        'navigateByUrl',
        'navigate'
      ]
    );

    router.navigateByUrl.and.returnValue(
      Promise.resolve(true)
    );

    router.navigate.and.returnValue(
      Promise.resolve(true)
    );

    component = new TravelInfoFormComponent(
      new FormBuilder(),
      candidateFormService,
      countryService,
      router
    );
  });

  it('should create with the initial state', () => {
    expect(component).toBeTruthy();
    expect(component.form).toBeNull();
    expect(component.error).toBeNull();
    expect(component.submitting).toBeFalse();
    expect(component.countries).toEqual([]);
    expect(component.readOnly).toBeFalse();
    expect(component.candidate).toBeNull();
  });

  it('should load countries and initialise the form', () => {
    countryService.listCountries.and.returnValue(
      of([afghanistan, italy])
    );

    candidateFormService.getTravelInfoForm.and.returnValue(
      of(createFormData())
    );

    component.ngOnInit();

    expect(countryService.listCountries)
    .toHaveBeenCalledTimes(1);

    expect(component.countries).toEqual([
      afghanistan,
      italy
    ]);

    expect(component.form).toBeTruthy();

    expect(candidateFormService.getTravelInfoForm)
    .toHaveBeenCalledTimes(1);
  });

  it('should initialise the form when loading countries fails', () => {
    const countries$ = new Subject<Country[]>();

    countryService.listCountries.and.returnValue(
      countries$
    );

    candidateFormService.getTravelInfoForm.and.returnValue(
      of(createFormData())
    );

    component.ngOnInit();

    countries$.error(
      new Error('Country request failed')
    );

    expect(component.countries).toEqual([]);
    expect(component.form).toBeTruthy();

    expect(candidateFormService.getTravelInfoForm)
    .toHaveBeenCalledTimes(1);
  });

  it('should patch previously saved form data', () => {
    const data = createFormData();

    countryService.listCountries.and.returnValue(
      of([afghanistan, italy])
    );

    candidateFormService.getTravelInfoForm.and.returnValue(
      of(data)
    );

    component.ngOnInit();

    expect(component.form?.get('firstName')?.value)
    .toBe('Ehsan');

    expect(component.form?.get('lastName')?.value)
    .toBe('Ehrari');

    expect(component.form?.get('travelDocNumber')?.value)
    .toBe('P123456');

    expect(component.form?.get('birthCountry')?.value)
    .toBe(afghanistan);
  });

  it('should set birthCountry to null when no matching country exists', () => {
    const data = createFormData();

    data.birthCountry = {
      id: 999,
      name: 'Unknown'
    } as Country;

    countryService.listCountries.and.returnValue(
      of([afghanistan, italy])
    );

    candidateFormService.getTravelInfoForm.and.returnValue(
      of(data)
    );

    component.ngOnInit();

    expect(
      component.form?.get('birthCountry')?.value
    ).toBeNull();
  });

  it('should reset the form when loading saved data fails', () => {
    const response$ =
      new Subject<TravelInfoFormData>();

    countryService.listCountries.and.returnValue(
      of([afghanistan])
    );

    candidateFormService.getTravelInfoForm.and.returnValue(
      response$
    );

    component.ngOnInit();

    const resetSpy = spyOn(
      component.form!,
      'reset'
    ).and.callThrough();

    response$.error(
      new Error('Unable to load saved form')
    );

    expect(resetSpy).toHaveBeenCalledTimes(1);
  });

  it('should disable all form controls in read-only mode', () => {
    component.readOnly = true;

    initialiseForm();

    Object.values(component.form!.controls)
    .forEach(control => {
      expect(control.disabled).toBeTrue();
    });
  });

  it('should leave controls enabled when not read-only', () => {
    component.readOnly = false;

    initialiseForm();

    Object.values(component.form!.controls)
    .forEach(control => {
      expect(control.enabled).toBeTrue();
    });
  });

  it('should validate required fields', () => {
    initialiseEmptyForm();

    expect(component.form?.invalid).toBeTrue();

    expect(
      component.form
      ?.get('firstName')
      ?.hasError('required')
    ).toBeTrue();

    expect(
      component.form
      ?.get('lastName')
      ?.hasError('required')
    ).toBeTrue();

    expect(
      component.form
      ?.get('dateOfBirth')
      ?.hasError('required')
    ).toBeTrue();

    expect(
      component.form
      ?.get('travelDocType')
      ?.hasError('required')
    ).toBeTrue();

    expect(
      component.form
      ?.get('travelDocNumber')
      ?.hasError('required')
    ).toBeTrue();
  });

  it('should detect profile mismatches', () => {
    component.candidate = {
      user: {
        firstName: 'Ehsan',
        lastName: 'Ehrari'
      },
      dob: '1995-01-01',
      gender: 'male',
      birthCountry: {
        id: 1,
        name: 'Afghanistan'
      }
    };

    initialiseForm();

    component.form?.patchValue({
      firstName: 'Different',
      lastName: 'Ehrari',
      dateOfBirth: '1995-01-01',
      gender: 'male',
      birthCountry: afghanistan,
      travelDocType: 'Passport',
      travelDocNumber: 'P123456'
    });

    component.form?.updateValueAndValidity();

    expect(component.hasProfileMismatch).toBeTrue();

    expect(component.mismatchFields)
    .toContain('firstName');

    expect(
      component.controlHasProfileMismatch('firstName')
    ).toBeTrue();
  });

  it('should return no mismatch fields without form errors', () => {
    initialiseForm();

    expect(component.mismatchFields).toEqual([]);
    expect(component.hasProfileMismatch).toBeFalse();
  });

  it('should return no mismatch fields when form is null', () => {
    component.form = null;

    expect(component.mismatchFields).toEqual([]);
    expect(component.hasProfileMismatch).toBeFalse();
  });

  it('should return mismatch field names from form errors', () => {
    initialiseForm();

    component.form?.setErrors({
      mismatch: {
        firstName: true,
        dateOfBirth: true
      }
    });

    expect(component.mismatchFields).toEqual([
      'firstName',
      'dateOfBirth'
    ]);

    expect(component.hasProfileMismatch).toBeTrue();
  });

  it('should detect a mismatch error on a control', () => {
    initialiseForm();

    component.form?.get('firstName')?.setErrors({
      mismatch: true
    });

    expect(
      component.controlHasProfileMismatch('firstName')
    ).toBeTrue();

    expect(
      component.controlHasProfileMismatch('lastName')
    ).toBeFalse();
  });

  it('should return false for control mismatch without a form', () => {
    component.form = null;

    expect(
      component.controlHasProfileMismatch('firstName')
    ).toBeFalse();
  });

  it('should compare countries by id', () => {
    const sameCountry = {
      id: 1,
      name: 'Afghanistan copy'
    } as Country;

    expect(
      component.compareCountry(
        afghanistan,
        sameCountry
      )
    ).toBeTrue();

    expect(
      component.compareCountry(
        afghanistan,
        italy
      )
    ).toBeFalse();
  });

  it('should compare null country values by identity', () => {
    expect(
      component.compareCountry(
        null as any,
        null as any
      )
    ).toBeTrue();

    expect(
      component.compareCountry(
        afghanistan,
        null as any
      )
    ).toBeFalse();

    expect(
      component.compareCountry(
        null as any,
        afghanistan
      )
    ).toBeFalse();
  });

  it('should navigate to the profile tab', async () => {
    component.goToProfile();

    expect(router.navigateByUrl)
    .toHaveBeenCalledOnceWith(
      '/',
      {
        skipLocationChange: true
      }
    );

    await Promise.resolve();

    expect(router.navigate)
    .toHaveBeenCalledOnceWith(
      ['/profile'],
      {
        queryParams: {
          tab: 'Profile'
        }
      }
    );
  });

  it('should return false from hasError without a form', () => {
    component.form = null;

    expect(
      component.hasError(
        'firstName',
        'required'
      )
    ).toBeFalse();
  });

  it('should return false for an untouched invalid control', () => {
    initialiseEmptyForm();

    expect(
      component.hasError(
        'firstName',
        'required'
      )
    ).toBeFalse();
  });

  it('should return true for a touched invalid control', () => {
    initialiseEmptyForm();

    const control =
      component.form?.get('firstName');

    control?.markAsTouched();

    expect(control?.hasError('required')).toBeTrue();

    expect(
      component.hasError(
        'firstName',
        'required'
      )
    ).toBeTrue();
  });

  it('should hide validation errors in read-only mode', () => {
    component.readOnly = true;

    initialiseEmptyForm();

    component.form
    ?.get('firstName')
    ?.markAsTouched();

    expect(
      component.hasError(
        'firstName',
        'required'
      )
    ).toBeFalse();
  });

  it('should return false for an unknown control', () => {
    initialiseEmptyForm();

    expect(
      component.hasError(
        'unknownControl' as keyof TravelInfoFormData,
        'required'
      )
    ).toBeFalse();
  });

  it('should allow a valid form to be submitted', () => {
    initialiseEmptyForm();
    setValidFormValues();

    expect(component.canSubmit()).toBeTrue();
  });

  it('should not submit when the form has not been created', () => {
    component.form = null;

    expect(component.canSubmit()).toBeFalse();
  });

  it('should not submit an invalid form', () => {
    initialiseEmptyForm();

    expect(component.form?.invalid).toBeTrue();
    expect(component.canSubmit()).toBeFalse();
  });

  it('should not submit while the form is pending', () => {
    initialiseEmptyForm();
    setValidFormValues();

    component.form?.markAsPending();

    expect(component.canSubmit()).toBeFalse();
  });

  it('should not submit while another submission is running', () => {
    initialiseEmptyForm();
    setValidFormValues();

    component.submitting = true;

    expect(component.canSubmit()).toBeFalse();
  });

  it('should not submit in read-only mode', () => {
    initialiseEmptyForm();
    setValidFormValues();

    component.readOnly = true;

    expect(component.canSubmit()).toBeFalse();
  });

  it('should not submit when profile fields mismatch', () => {
    initialiseEmptyForm();
    setValidFormValues();

    component.form?.setErrors({
      mismatch: {
        firstName: true
      }
    });

    expect(component.canSubmit()).toBeFalse();
  });

  it('should mark controls as touched when submission is invalid', () => {
    initialiseEmptyForm();

    expect(component.form?.invalid).toBeTrue();

    component.onSubmit();

    expect(
      component.form?.get('firstName')?.touched
    ).toBeTrue();

    expect(
      component.form?.get('lastName')?.touched
    ).toBeTrue();

    expect(
      component.form?.get('dateOfBirth')?.touched
    ).toBeTrue();

    expect(
      component.form?.get('travelDocType')?.touched
    ).toBeTrue();

    expect(
      component.form?.get('travelDocNumber')?.touched
    ).toBeTrue();

    expect(
      candidateFormService
        .createOrUpdateTravelInfoForm
    ).not.toHaveBeenCalled();
  });

  it('should safely handle submission before form creation', () => {
    component.form = null;

    expect(() => component.onSubmit()).not.toThrow();

    expect(
      candidateFormService
        .createOrUpdateTravelInfoForm
    ).not.toHaveBeenCalled();
  });

  it('should submit valid form data', () => {
    const response$ =
      new Subject<TravelInfoFormData>();

    initialiseEmptyForm();
    setValidFormValues();

    candidateFormService
    .createOrUpdateTravelInfoForm
    .and.returnValue(response$);

    component.error = 'Previous error';

    component.onSubmit();

    expect(component.submitting).toBeTrue();
    expect(component.error).toBeNull();

    expect(
      candidateFormService
        .createOrUpdateTravelInfoForm
    ).toHaveBeenCalledTimes(1);

    const request =
      candidateFormService
      .createOrUpdateTravelInfoForm
      .calls.mostRecent().args[0];

    expect(request.firstName).toBe('Ehsan');
    expect(request.lastName).toBe('Ehrari');
    expect(request.travelDocNumber).toBe('P123456');
  });

  it('should emit saved data and reset form state after success', () => {
    const response$ =
      new Subject<TravelInfoFormData>();

    const savedData = createFormData();

    initialiseEmptyForm();
    setValidFormValues();

    candidateFormService
    .createOrUpdateTravelInfoForm
    .and.returnValue(response$);

    const emittedSpy = spyOn(
      component.submitted,
      'emit'
    );

    component.form?.markAsDirty();
    component.form?.markAllAsTouched();

    component.onSubmit();

    response$.next(savedData);

    expect(emittedSpy)
    .toHaveBeenCalledOnceWith(savedData);

    expect(component.form?.pristine).toBeTrue();
    expect(component.form?.untouched).toBeTrue();
    expect(component.submitting).toBeFalse();
  });

  it('should store the error when submission fails', () => {
    const response$ =
      new Subject<TravelInfoFormData>();

    const error = new Error(
      'Unable to save travel information'
    );

    initialiseEmptyForm();
    setValidFormValues();

    candidateFormService
    .createOrUpdateTravelInfoForm
    .and.returnValue(response$);

    component.onSubmit();

    expect(component.submitting).toBeTrue();

    response$.error(error);

    expect(component.error).toBe(error);
    expect(component.submitting).toBeFalse();
  });

  function initialiseForm(
    data: TravelInfoFormData = createFormData()
  ): void {
    countryService.listCountries.and.returnValue(
      of([afghanistan, italy])
    );

    candidateFormService.getTravelInfoForm.and.returnValue(
      of(data)
    );

    component.ngOnInit();
  }

  function initialiseEmptyForm(): void {
    countryService.listCountries.and.returnValue(
      of([afghanistan, italy])
    );

    candidateFormService.getTravelInfoForm.and.returnValue(
      of({} as TravelInfoFormData)
    );

    component.ngOnInit();
  }

  function setValidFormValues(): void {
    component.form?.patchValue({
      firstName: 'Ehsan',
      lastName: 'Ehrari',
      dateOfBirth: '1995-01-01',
      gender: 'male',
      birthCountry: afghanistan,
      placeOfBirth: 'Kabul',
      travelDocType: 'Passport' as TravelDocType,
      travelDocNumber: 'P123456',
      travelDocIssuedBy: 'Afghanistan',
      travelDocIssueDate: '2025-01-01',
      travelDocExpiryDate: '2030-01-01',
      travelInfoComment: 'Test comment'
    });

    component.form?.updateValueAndValidity();
  }
});

function createFormData(): TravelInfoFormData {
  return {
    firstName: 'Ehsan',
    lastName: 'Ehrari',
    dateOfBirth: '1995-01-01',
    gender: 'male',
    birthCountry: {
      id: 1,
      name: 'Afghanistan'
    } as Country,
    placeOfBirth: 'Kabul',
    travelDocType: 'Passport' as TravelDocType,
    travelDocNumber: 'P123456',
    travelDocIssuedBy: 'Afghanistan',
    travelDocIssueDate: '2025-01-01',
    travelDocExpiryDate: '2030-01-01',
    travelInfoComment: 'Test comment'
  } as TravelInfoFormData;
}

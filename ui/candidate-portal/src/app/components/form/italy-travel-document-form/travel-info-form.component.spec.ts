import type { MockedObject } from "vitest";
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

import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { of, Subject } from 'rxjs';

import { Country } from '../../../model/country';
import { TravelDocType, TravelInfoFormData } from '../../../model/form';
import { CandidateFormService } from '../../../services/candidate-form.service';
import { CountryService } from '../../../services/country.service';
import { TravelInfoFormComponent } from './travel-info-form.component';

describe('TravelInfoFormComponent', () => {
    let component: TravelInfoFormComponent;
    let candidateFormService: MockedObject<CandidateFormService>;
    let countryService: MockedObject<CountryService>;
    let router: MockedObject<Router>;

    const afghanistan = {
        id: 1,
        name: 'Afghanistan'
    } as Country;

    const italy = {
        id: 2,
        name: 'Italy'
    } as Country;

    beforeEach(() => {
        candidateFormService = {
            getTravelInfoForm: vi.fn().mockName("CandidateFormService.getTravelInfoForm"),
            createOrUpdateTravelInfoForm: vi.fn().mockName("CandidateFormService.createOrUpdateTravelInfoForm")
        } as unknown as MockedObject<CandidateFormService>;

        countryService = {
            listCountries: vi.fn().mockName("CountryService.listCountries")
        } as unknown as MockedObject<CountryService>;

        router = {
            navigateByUrl: vi.fn().mockName("Router.navigateByUrl"),
            navigate: vi.fn().mockName("Router.navigate")
        } as unknown as MockedObject<Router>;

        router.navigateByUrl.mockResolvedValue(true);

        router.navigate.mockResolvedValue(true);

        component = new TravelInfoFormComponent(new FormBuilder(), candidateFormService, countryService, router);
    });

    it('should create with the initial state', () => {
        expect(component).toBeTruthy();
        expect(component.form).toBeNull();
        expect(component.error).toBeNull();
        expect(component.submitting).toBe(false);
        expect(component.countries).toEqual([]);
        expect(component.readOnly).toBe(false);
        expect(component.candidate).toBeNull();
    });

    it('should load countries and initialise the form', () => {
        countryService.listCountries.mockReturnValue(of([afghanistan, italy]));

        candidateFormService.getTravelInfoForm.mockReturnValue(of(createFormData()));

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

        countryService.listCountries.mockReturnValue(countries$);

        candidateFormService.getTravelInfoForm.mockReturnValue(of(createFormData()));

        component.ngOnInit();

        countries$.error(new Error('Country request failed'));

        expect(component.countries).toEqual([]);
        expect(component.form).toBeTruthy();

        expect(candidateFormService.getTravelInfoForm)
            .toHaveBeenCalledTimes(1);
    });

    it('should patch previously saved form data', () => {
        const data = createFormData();

        countryService.listCountries.mockReturnValue(of([afghanistan, italy]));

        candidateFormService.getTravelInfoForm.mockReturnValue(of(data));

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

        countryService.listCountries.mockReturnValue(of([afghanistan, italy]));

        candidateFormService.getTravelInfoForm.mockReturnValue(of(data));

        component.ngOnInit();

        expect(component.form?.get('birthCountry')?.value).toBeNull();
    });

    it('should reset the form when loading saved data fails', () => {
        const response$ = new Subject<TravelInfoFormData>();

        countryService.listCountries.mockReturnValue(of([afghanistan]));

        candidateFormService.getTravelInfoForm.mockReturnValue(response$);

        component.ngOnInit();

        const resetSpy = vi.spyOn(component.form!, 'reset');

        response$.error(new Error('Unable to load saved form'));

        expect(resetSpy).toHaveBeenCalledTimes(1);
    });

    it('should disable all form controls in read-only mode', () => {
        component.readOnly = true;

        initialiseForm();

        Object.values(component.form!.controls)
            .forEach(control => {
            expect(control.disabled).toBe(true);
        });
    });

    it('should leave controls enabled when not read-only', () => {
        component.readOnly = false;

        initialiseForm();

        Object.values(component.form!.controls)
            .forEach(control => {
            expect(control.enabled).toBe(true);
        });
    });

    it('should validate required fields', () => {
        initialiseEmptyForm();

        expect(component.form?.invalid).toBe(true);

        expect(component.form
            ?.get('firstName')
            ?.hasError('required')).toBe(true);

        expect(component.form
            ?.get('lastName')
            ?.hasError('required')).toBe(true);

        expect(component.form
            ?.get('dateOfBirth')
            ?.hasError('required')).toBe(true);

        expect(component.form
            ?.get('travelDocType')
            ?.hasError('required')).toBe(true);

        expect(component.form
            ?.get('travelDocNumber')
            ?.hasError('required')).toBe(true);
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

        expect(component.hasProfileMismatch).toBe(true);

        expect(component.mismatchFields)
            .toContain('firstName');

        expect(component.controlHasProfileMismatch('firstName')).toBe(true);
    });

    it('should return no mismatch fields without form errors', () => {
        initialiseForm();

        expect(component.mismatchFields).toEqual([]);
        expect(component.hasProfileMismatch).toBe(false);
    });

    it('should return no mismatch fields when form is null', () => {
        component.form = null;

        expect(component.mismatchFields).toEqual([]);
        expect(component.hasProfileMismatch).toBe(false);
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

        expect(component.hasProfileMismatch).toBe(true);
    });

    it('should detect a mismatch error on a control', () => {
        initialiseForm();

        component.form?.get('firstName')?.setErrors({
            mismatch: true
        });

        expect(component.controlHasProfileMismatch('firstName')).toBe(true);

        expect(component.controlHasProfileMismatch('lastName')).toBe(false);
    });

    it('should return false for control mismatch without a form', () => {
        component.form = null;

        expect(component.controlHasProfileMismatch('firstName')).toBe(false);
    });

    it('should compare countries by id', () => {
        const sameCountry = {
            id: 1,
            name: 'Afghanistan copy'
        } as Country;

        expect(component.compareCountry(afghanistan, sameCountry)).toBe(true);

        expect(component.compareCountry(afghanistan, italy)).toBe(false);
    });

    it('should compare null country values by identity', () => {
        expect(component.compareCountry(null as any, null as any)).toBe(true);

        expect(component.compareCountry(afghanistan, null as any)).toBe(false);

        expect(component.compareCountry(null as any, afghanistan)).toBe(false);
    });

    it('should navigate to the profile tab', async () => {
        component.goToProfile();

        expect(router.navigateByUrl).toHaveBeenCalledTimes(1);

        expect(router.navigateByUrl).toHaveBeenCalledWith('/', {
            skipLocationChange: true
        });

        await Promise.resolve();

        expect(router.navigate).toHaveBeenCalledTimes(1);

        expect(router.navigate).toHaveBeenCalledWith(['/profile'], {
            queryParams: {
                tab: 'Profile'
            }
        });
    });

    it('should return false from hasError without a form', () => {
        component.form = null;

        expect(component.hasError('firstName', 'required')).toBe(false);
    });

    it('should return false for an untouched invalid control', () => {
        initialiseEmptyForm();

        expect(component.hasError('firstName', 'required')).toBe(false);
    });

    it('should return true for a touched invalid control', () => {
        initialiseEmptyForm();

        const control = component.form?.get('firstName');

        control?.markAsTouched();

        expect(control?.hasError('required')).toBe(true);

        expect(component.hasError('firstName', 'required')).toBe(true);
    });

    it('should hide validation errors in read-only mode', () => {
        component.readOnly = true;

        initialiseEmptyForm();

        component.form
            ?.get('firstName')
            ?.markAsTouched();

        expect(component.hasError('firstName', 'required')).toBe(false);
    });

    it('should return false for an unknown control', () => {
        initialiseEmptyForm();

        expect(component.hasError('unknownControl' as keyof TravelInfoFormData, 'required')).toBe(false);
    });

    it('should allow a valid form to be submitted', () => {
        initialiseEmptyForm();
        setValidFormValues();

        expect(component.canSubmit()).toBe(true);
    });

    it('should not submit when the form has not been created', () => {
        component.form = null;

        expect(component.canSubmit()).toBe(false);
    });

    it('should not submit an invalid form', () => {
        initialiseEmptyForm();

        expect(component.form?.invalid).toBe(true);
        expect(component.canSubmit()).toBe(false);
    });

    it('should not submit while the form is pending', () => {
        initialiseEmptyForm();
        setValidFormValues();

        component.form?.markAsPending();

        expect(component.canSubmit()).toBe(false);
    });

    it('should not submit while another submission is running', () => {
        initialiseEmptyForm();
        setValidFormValues();

        component.submitting = true;

        expect(component.canSubmit()).toBe(false);
    });

    it('should not submit in read-only mode', () => {
        initialiseEmptyForm();
        setValidFormValues();

        component.readOnly = true;

        expect(component.canSubmit()).toBe(false);
    });

    it('should not submit when profile fields mismatch', () => {
        initialiseEmptyForm();
        setValidFormValues();

        component.form?.setErrors({
            mismatch: {
                firstName: true
            }
        });

        expect(component.canSubmit()).toBe(false);
    });

    it('should mark controls as touched when submission is invalid', () => {
        initialiseEmptyForm();

        expect(component.form?.invalid).toBe(true);

        component.onSubmit();

        expect(component.form?.get('firstName')?.touched).toBe(true);

        expect(component.form?.get('lastName')?.touched).toBe(true);

        expect(component.form?.get('dateOfBirth')?.touched).toBe(true);

        expect(component.form?.get('travelDocType')?.touched).toBe(true);

        expect(component.form?.get('travelDocNumber')?.touched).toBe(true);

        expect(candidateFormService
            .createOrUpdateTravelInfoForm).not.toHaveBeenCalled();
    });

    it('should safely handle submission before form creation', () => {
        component.form = null;

        expect(() => component.onSubmit()).not.toThrow();

        expect(candidateFormService
            .createOrUpdateTravelInfoForm).not.toHaveBeenCalled();
    });

    it('should submit valid form data', () => {
        const response$ = new Subject<TravelInfoFormData>();

        initialiseEmptyForm();
        setValidFormValues();

        candidateFormService
            .createOrUpdateTravelInfoForm.mockReturnValue(response$);

        component.error = 'Previous error';

        component.onSubmit();

        expect(component.submitting).toBe(true);
        expect(component.error).toBeNull();

        expect(candidateFormService
            .createOrUpdateTravelInfoForm).toHaveBeenCalledTimes(1);

        const request = vi.mocked(candidateFormService
            .createOrUpdateTravelInfoForm).mock.lastCall[0];

        expect(request.firstName).toBe('Ehsan');
        expect(request.lastName).toBe('Ehrari');
        expect(request.travelDocNumber).toBe('P123456');
    });

    it('should emit saved data and reset form state after success', () => {
        const response$ = new Subject<TravelInfoFormData>();

        const savedData = createFormData();

        initialiseEmptyForm();
        setValidFormValues();

        candidateFormService
            .createOrUpdateTravelInfoForm.mockReturnValue(response$);

        const emittedSpy = vi.spyOn(component.submitted, 'emit').mockReturnValue(undefined);

        component.form?.markAsDirty();
        component.form?.markAllAsTouched();

        component.onSubmit();

        response$.next(savedData);

        expect(emittedSpy).toHaveBeenCalledTimes(1);

        expect(emittedSpy).toHaveBeenCalledWith(savedData);

        expect(component.form?.pristine).toBe(true);
        expect(component.form?.untouched).toBe(true);
        expect(component.submitting).toBe(false);
    });

    it('should store the error when submission fails', () => {
        const response$ = new Subject<TravelInfoFormData>();

        const error = new Error('Unable to save travel information');

        initialiseEmptyForm();
        setValidFormValues();

        candidateFormService
            .createOrUpdateTravelInfoForm.mockReturnValue(response$);

        component.onSubmit();

        expect(component.submitting).toBe(true);

        response$.error(error);

        expect(component.error).toBe(error);
        expect(component.submitting).toBe(false);
    });

    function initialiseForm(data: TravelInfoFormData = createFormData()): void {
        countryService.listCountries.mockReturnValue(of([afghanistan, italy]));

        candidateFormService.getTravelInfoForm.mockReturnValue(of(data));

        component.ngOnInit();
    }

    function initialiseEmptyForm(): void {
        countryService.listCountries.mockReturnValue(of([afghanistan, italy]));

        candidateFormService.getTravelInfoForm.mockReturnValue(of({} as TravelInfoFormData));

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

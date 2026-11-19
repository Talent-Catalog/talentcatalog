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

import type {MockedObject} from "vitest";
import {FormBuilder} from '@angular/forms';
import {of, Subject} from 'rxjs';

import {MyFirstFormData} from '../../../model/form';
import {CandidateFormService} from '../../../services/candidate-form.service';
import {MyFirstFormComponent} from './my-first-form.component';

describe('MyFirstFormComponent', () => {
    let component: MyFirstFormComponent;
    let candidateFormService: MockedObject<CandidateFormService>;

    beforeEach(() => {
        candidateFormService = {
            getMyFirstForm: vi.fn().mockName("CandidateFormService.getMyFirstForm"),
            createOrUpdateMyFirstForm: vi.fn().mockName("CandidateFormService.createOrUpdateMyFirstForm")
        } as unknown as MockedObject<CandidateFormService>;

        component = new MyFirstFormComponent(candidateFormService, new FormBuilder());
    });

    it('should create with the initial state', () => {
        expect(component).toBeTruthy();
        expect(component.form).toBeUndefined();
        expect(component.error).toBeNull();
        expect(component.submitting).toBe(false);
        expect(component.readOnly).toBe(false);
        expect(component.candidate).toBeNull();
    });

    it('should initialise the form', () => {
        candidateFormService.getMyFirstForm.mockReturnValue(of(createFormData()));

        component.ngOnInit();

        expect(component.form).toBeTruthy();

        expect(component.form.contains('city')).toBe(true);

        expect(component.form.contains('hairColour')).toBe(true);

        expect(candidateFormService.getMyFirstForm).toHaveBeenCalledTimes(1);
    });

    it('should load the previously saved form data', () => {
        candidateFormService.getMyFirstForm.mockReturnValue(of({
            city: 'Kabul',
            hairColour: 'Black'
        } as MyFirstFormData));

        component.ngOnInit();

        expect(component.form.get('city')?.value).toBe('Kabul');

        expect(component.form.get('hairColour')?.value).toBe('Black');
    });

    it('should clear an existing error during initialisation', () => {
        component.error = new Error('Previous error');

        candidateFormService.getMyFirstForm.mockReturnValue(of(createFormData()));

        component.ngOnInit();

        expect(component.error).toBeNull();
    });

    it('should reset to empty values when loading fails', () => {
        const response$ = new Subject<MyFirstFormData>();

        candidateFormService.getMyFirstForm.mockReturnValue(response$);

        component.ngOnInit();

        component.form.patchValue({
            city: 'Temporary city',
            hairColour: 'Temporary colour'
        });

        response$.error(new Error('Form does not exist'));

        expect(component.form.get('city')?.value).toBe('');

        expect(component.form.get('hairColour')?.value).toBe('');

        expect(component.form.invalid).toBe(true);
    });

    it('should apply required validation to city', () => {
        initialiseEmptyForm();

        expect(component.form
            .get('city')
            ?.hasError('required')).toBe(true);
    });

    it('should apply required validation to hairColour', () => {
        initialiseEmptyForm();

        expect(component.form
            .get('hairColour')
            ?.hasError('required')).toBe(true);
    });

    it('should be valid when required fields have values', () => {
        initialiseEmptyForm();

        setValidFormValues();

        expect(component.form.valid).toBe(true);
    });

    describe('canSubmit', () => {
        it('should return true for a valid editable form', () => {
            initialiseEmptyForm();
            setValidFormValues();

            expect(component.canSubmit()).toBe(true);
        });

        it('should return false when the form is invalid', () => {
            initialiseEmptyForm();

            expect(component.form.invalid).toBe(true);
            expect(component.canSubmit()).toBe(false);
        });

        it('should return false while validation is pending', () => {
            initialiseEmptyForm();
            setValidFormValues();

            component.form.markAsPending();

            expect(component.canSubmit()).toBe(false);
        });

        it('should return false while submitting', () => {
            initialiseEmptyForm();
            setValidFormValues();

            component.submitting = true;

            expect(component.canSubmit()).toBe(false);
        });

        it('should return false in read-only mode', () => {
            initialiseEmptyForm();
            setValidFormValues();

            component.readOnly = true;

            expect(component.canSubmit()).toBe(false);
        });
    });

    describe('hasError', () => {
        it('should return false for an untouched invalid control', () => {
            initialiseEmptyForm();

            expect(component.hasError('city', 'required')).toBe(false);
        });

        it('should return true for a touched invalid city control', () => {
            initialiseEmptyForm();

            component.form
                .get('city')
                ?.markAsTouched();

            expect(component.form
                .get('city')
                ?.hasError('required')).toBe(true);

            expect(component.hasError('city', 'required')).toBe(true);
        });

        it('should return true for a touched invalid hairColour control', () => {
            initialiseEmptyForm();

            component.form
                .get('hairColour')
                ?.markAsTouched();

            expect(component.hasError('hairColour', 'required')).toBe(true);
        });

        it('should return false when the control is valid', () => {
            initialiseEmptyForm();

            component.form
                .get('city')
                ?.setValue('Kabul');

            component.form
                .get('city')
                ?.markAsTouched();

            expect(component.hasError('city', 'required')).toBe(false);
        });

        it('should return false in read-only mode', () => {
            component.readOnly = true;

            initialiseEmptyForm();

            component.form
                .get('city')
                ?.markAsTouched();

            expect(component.hasError('city', 'required')).toBe(false);
        });

        it('should return false for an unknown control', () => {
            initialiseEmptyForm();

            expect(component.hasError('unknown' as keyof MyFirstFormData, 'required')).toBe(false);
        });

        it('should return false for a different validation error', () => {
            initialiseEmptyForm();

            component.form
                .get('city')
                ?.markAsTouched();

            expect(component.hasError('city', 'maxlength')).toBe(false);
        });
    });

    describe('onSubmit', () => {
        it('should mark all controls touched when invalid', () => {
            initialiseEmptyForm();

            expect(component.form.invalid).toBe(true);

            component.onSubmit();

            expect(component.form.get('city')?.touched).toBe(true);

            expect(component.form.get('hairColour')?.touched).toBe(true);

            expect(candidateFormService
                .createOrUpdateMyFirstForm).not.toHaveBeenCalled();
        });

        it('should submit the form values', () => {
            const response$ = new Subject<MyFirstFormData>();

            initialiseEmptyForm();
            setValidFormValues();

            candidateFormService
                .createOrUpdateMyFirstForm.mockReturnValue(response$);

            component.error = new Error('Previous error');

            component.onSubmit();

            expect(component.submitting).toBe(true);
            expect(component.error).toBeNull();

            expect(candidateFormService
                .createOrUpdateMyFirstForm).toHaveBeenCalledTimes(1);

            const request = vi.mocked(candidateFormService
                .createOrUpdateMyFirstForm).mock.lastCall[0];

            expect(request).toEqual({
                city: 'Kabul',
                hairColour: 'Black'
            } as MyFirstFormData);
        });

        it('should emit saved data after successful submission', () => {
            const response$ = new Subject<MyFirstFormData>();

            const savedData = {
                city: 'Kabul',
                hairColour: 'Black'
            } as MyFirstFormData;

            initialiseEmptyForm();
            setValidFormValues();

            candidateFormService
                .createOrUpdateMyFirstForm.mockReturnValue(response$);

            const submittedSpy = vi.spyOn(component.submitted, 'emit').mockReturnValue(undefined);

            component.onSubmit();

            response$.next(savedData);

            expect(submittedSpy).toHaveBeenCalledTimes(1);

            expect(submittedSpy).toHaveBeenCalledWith(savedData);

            expect(component.submitting).toBe(false);
        });

        it('should mark the form pristine and untouched after success', () => {
            const response$ = new Subject<MyFirstFormData>();

            initialiseEmptyForm();
            setValidFormValues();

            candidateFormService
                .createOrUpdateMyFirstForm.mockReturnValue(response$);

            component.form.markAsDirty();
            component.form.markAllAsTouched();

            expect(component.form.dirty).toBe(true);
            expect(component.form.touched).toBe(true);

            component.onSubmit();

            response$.next(createFormData());

            expect(component.form.pristine).toBe(true);
            expect(component.form.untouched).toBe(true);
            expect(component.submitting).toBe(false);
        });

        it('should store submission errors', () => {
            const response$ = new Subject<MyFirstFormData>();

            const error = new Error('Unable to save form');

            initialiseEmptyForm();
            setValidFormValues();

            candidateFormService
                .createOrUpdateMyFirstForm.mockReturnValue(response$);

            component.onSubmit();

            expect(component.submitting).toBe(true);

            response$.error(error);

            expect(component.error).toBe(error);
            expect(component.submitting).toBe(false);
        });
    });

    function initialiseEmptyForm(): void {
        candidateFormService.getMyFirstForm.mockReturnValue(of({} as MyFirstFormData));

        component.ngOnInit();
    }

    function setValidFormValues(): void {
        component.form.setValue({
            city: 'Kabul',
            hairColour: 'Black'
        });

        component.form.updateValueAndValidity();
    }
});

function createFormData(): MyFirstFormData {
    return {
        city: 'Kabul',
        hairColour: 'Black'
    } as MyFirstFormData;
}

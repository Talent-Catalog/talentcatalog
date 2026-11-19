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

import {RefugeeStatusEvidenceDocumentType, RefugeeStatusInfoFormData, RsdRefugeeStatus} from '../../../model/form';
import {CandidateFormService} from '../../../services/candidate-form.service';
import {RefugeeStatusInfoFormComponent} from './refugee-status-info-form.component';

describe('RefugeeStatusInfoFormComponent', () => {
    let component: RefugeeStatusInfoFormComponent;
    let candidateFormService: MockedObject<CandidateFormService>;

    beforeEach(() => {
        candidateFormService = {
            getRefugeeStatusInfoForm: vi.fn().mockName("CandidateFormService.getRefugeeStatusInfoForm"),
            createOrUpdateRefugeeStatusInfoForm: vi.fn().mockName("CandidateFormService.createOrUpdateRefugeeStatusInfoForm")
        } as unknown as MockedObject<CandidateFormService>;

        component =
            new RefugeeStatusInfoFormComponent(candidateFormService, new FormBuilder());
    });

    it('should create with the initial state', () => {
        expect(component).toBeTruthy();
        expect(component.form).toBeUndefined();
        expect(component.error).toBeNull();
        expect(component.submitting).toBe(false);
        expect(component.readOnly).toBe(false);
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
                value: RefugeeStatusEvidenceDocumentType
                    .UnhcrCertificate,
                label: 'UNHCR Certificate of Recognition'
            },
            {
                value: RefugeeStatusEvidenceDocumentType
                    .HostCountryId,
                label: 'Host Country Refugee ID or Residence Permit'
            },
            {
                value: RefugeeStatusEvidenceDocumentType
                    .OfficialCampRegistration,
                label: 'Registration Document from Official Refugee Camp'
            }
        ]);
    });

    it('should initialise the form controls', () => {
        initialiseEmptyForm();

        expect(component.form).toBeTruthy();

        expect(component.form.contains('refugeeStatus')).toBe(true);

        expect(component.form.contains('documentType')).toBe(true);

        expect(component.form.contains('documentNumber')).toBe(true);

        expect(component.form.contains('refugeeStatusComment')).toBe(true);

        expect(candidateFormService.getRefugeeStatusInfoForm).toHaveBeenCalledTimes(1);
    });

    it('should load previously saved form data', () => {
        const savedData = createFormData();

        initialiseForm(savedData);

        expect(component.form.get('refugeeStatus')?.value).toBe(savedData.refugeeStatus);

        expect(component.form.get('documentType')?.value).toBe(savedData.documentType);

        expect(component.form.get('documentNumber')?.value).toBe('DOC-123');

        expect(component.form
            .get('refugeeStatusComment')
            ?.value).toBe('Existing comment');
    });

    it('should clear an existing error during initialisation', () => {
        component.error = new Error('Previous error');

        initialiseForm();

        expect(component.error).toBeNull();
    });

    it('should reset the form when loading data fails', () => {
        const response$ = new Subject<RefugeeStatusInfoFormData>();

        candidateFormService
            .getRefugeeStatusInfoForm.mockReturnValue(response$);

        component.ngOnInit();

        component.form.patchValue({
            refugeeStatus: RsdRefugeeStatus.RecognizedByUnhcr,
            documentType: RefugeeStatusEvidenceDocumentType
                .UnhcrCertificate,
            documentNumber: 'TEMP-123',
            refugeeStatusComment: 'Temporary comment'
        });

        response$.error(new Error('Form does not exist'));

        expect(component.form.get('refugeeStatus')?.value).toBeNull();

        expect(component.form.get('documentType')?.value).toBeNull();

        expect(component.form.get('documentNumber')?.value).toBeNull();

        expect(component.form
            .get('refugeeStatusComment')
            ?.value).toBeNull();

        expect(component.form.invalid).toBe(true);
    });
    it('should disable the form in read-only mode', () => {
        component.readOnly = true;

        initialiseForm();

        expect(component.form.disabled).toBe(true);

        Object.values(component.form.controls)
            .forEach(control => {
            expect(control.disabled).toBe(true);
        });
    });

    it('should leave the form enabled in editable mode', () => {
        component.readOnly = false;

        initialiseForm();

        expect(component.form.enabled).toBe(true);

        Object.values(component.form.controls)
            .forEach(control => {
            expect(control.enabled).toBe(true);
        });
    });

    describe('validation', () => {
        it('should require refugee status', () => {
            initialiseEmptyForm();

            expect(component.form
                .get('refugeeStatus')
                ?.hasError('required')).toBe(true);
        });

        it('should require document type', () => {
            initialiseEmptyForm();

            expect(component.form
                .get('documentType')
                ?.hasError('required')).toBe(true);
        });

        it('should allow an empty document number', () => {
            initialiseEmptyForm();

            expect(component.form
                .get('documentNumber')
                ?.valid).toBe(true);
        });

        it('should allow a 30-character document number', () => {
            initialiseEmptyForm();

            component.form
                .get('documentNumber')
                ?.setValue('1'.repeat(30));

            expect(component.form
                .get('documentNumber')
                ?.valid).toBe(true);
        });

        it('should reject a document number longer than 30 characters', () => {
            initialiseEmptyForm();

            component.form
                .get('documentNumber')
                ?.setValue('1'.repeat(31));

            expect(component.form
                .get('documentNumber')
                ?.hasError('maxlength')).toBe(true);
        });

        it('should be valid when required fields are supplied', () => {
            initialiseEmptyForm();

            setValidFormValues();

            expect(component.form.valid).toBe(true);
        });
    });

    describe('canSubmit', () => {
        it('should return true for a valid editable form', () => {
            initialiseEmptyForm();
            setValidFormValues();

            expect(component.canSubmit()).toBe(true);
        });

        it('should return false for an invalid form', () => {
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
            component.readOnly = true;

            initialiseForm();

            expect(component.canSubmit()).toBe(false);
        });
    });

    describe('hasError', () => {
        it('should return false for an untouched invalid control', () => {
            initialiseEmptyForm();

            expect(component.hasError('refugeeStatus', 'required')).toBe(false);
        });

        it('should return true for a touched required control', () => {
            initialiseEmptyForm();

            const control = component.form.get('refugeeStatus');

            control?.markAsTouched();

            expect(control?.hasError('required')).toBe(true);

            expect(component.hasError('refugeeStatus', 'required')).toBe(true);
        });

        it('should detect the maximum-length error', () => {
            initialiseEmptyForm();

            const control = component.form.get('documentNumber');

            control?.setValue('1'.repeat(31));
            control?.markAsTouched();

            expect(component.hasError('documentNumber', 'maxlength')).toBe(true);
        });

        it('should return false for a valid control', () => {
            initialiseEmptyForm();

            const control = component.form.get('refugeeStatus');

            control?.setValue(RsdRefugeeStatus.RecognizedByUnhcr);

            control?.markAsTouched();

            expect(component.hasError('refugeeStatus', 'required')).toBe(false);
        });

        it('should return false in read-only mode', () => {
            component.readOnly = true;

            initialiseEmptyForm();

            component.form
                .get('refugeeStatus')
                ?.markAsTouched();

            expect(component.hasError('refugeeStatus', 'required')).toBe(false);
        });

        it('should return false for an unknown control', () => {
            initialiseEmptyForm();

            expect(component.hasError('unknown' as keyof RefugeeStatusInfoFormData, 'required')).toBe(false);
        });
    });

    describe('onSubmit', () => {
        it('should mark all controls touched when invalid', () => {
            initialiseEmptyForm();

            expect(component.form.invalid).toBe(true);

            component.onSubmit();

            expect(component.form
                .get('refugeeStatus')
                ?.touched).toBe(true);

            expect(component.form
                .get('documentType')
                ?.touched).toBe(true);

            expect(component.form
                .get('documentNumber')
                ?.touched).toBe(true);

            expect(component.form
                .get('refugeeStatusComment')
                ?.touched).toBe(true);

            expect(candidateFormService
                .createOrUpdateRefugeeStatusInfoForm).not.toHaveBeenCalled();
        });

        it('should submit raw form data', () => {
            const response$ = new Subject<RefugeeStatusInfoFormData>();

            initialiseEmptyForm();
            setValidFormValues();

            candidateFormService
                .createOrUpdateRefugeeStatusInfoForm.mockReturnValue(response$);

            component.error = new Error('Previous error');

            component.onSubmit();

            expect(component.submitting).toBe(true);
            expect(component.error).toBeNull();

            expect(candidateFormService
                .createOrUpdateRefugeeStatusInfoForm).toHaveBeenCalledTimes(1);

            const request = vi.mocked(candidateFormService
                .createOrUpdateRefugeeStatusInfoForm).mock.lastCall[0];

            expect(request).toEqual(createFormData());
        });

        it('should emit saved data after successful submission', () => {
            const response$ = new Subject<RefugeeStatusInfoFormData>();

            const savedData = createFormData();

            initialiseEmptyForm();
            setValidFormValues();

            candidateFormService
                .createOrUpdateRefugeeStatusInfoForm.mockReturnValue(response$);

            const submittedSpy = vi.spyOn(component.submitted, 'emit').mockReturnValue(undefined);

            component.onSubmit();

            response$.next(savedData);

            expect(submittedSpy).toHaveBeenCalledTimes(1);

            expect(submittedSpy).toHaveBeenCalledWith(savedData);

            expect(component.submitting).toBe(false);
        });

        it('should mark the form pristine and untouched after success', () => {
            const response$ = new Subject<RefugeeStatusInfoFormData>();

            initialiseEmptyForm();
            setValidFormValues();

            candidateFormService
                .createOrUpdateRefugeeStatusInfoForm.mockReturnValue(response$);

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
            const response$ = new Subject<RefugeeStatusInfoFormData>();

            const error = new Error('Unable to save refugee information');

            initialiseEmptyForm();
            setValidFormValues();

            candidateFormService
                .createOrUpdateRefugeeStatusInfoForm.mockReturnValue(response$);

            component.onSubmit();

            expect(component.submitting).toBe(true);

            response$.error(error);

            expect(component.error).toBe(error);
            expect(component.submitting).toBe(false);
        });
    });

    function initialiseForm(data: RefugeeStatusInfoFormData = createFormData()): void {
        candidateFormService
            .getRefugeeStatusInfoForm.mockReturnValue(of(data));

        component.ngOnInit();
    }

    function initialiseEmptyForm(): void {
        candidateFormService
            .getRefugeeStatusInfoForm.mockReturnValue(of({} as RefugeeStatusInfoFormData));

        component.ngOnInit();
    }

    function setValidFormValues(): void {
        component.form.setValue(createFormData());

        component.form.updateValueAndValidity();
    }
});

function createFormData(): RefugeeStatusInfoFormData {
    return {
        refugeeStatus: RsdRefugeeStatus.RecognizedByUnhcr,
        documentType: RefugeeStatusEvidenceDocumentType
            .UnhcrCertificate,
        documentNumber: 'DOC-123',
        refugeeStatusComment: 'Existing comment'
    } as RefugeeStatusInfoFormData;
}

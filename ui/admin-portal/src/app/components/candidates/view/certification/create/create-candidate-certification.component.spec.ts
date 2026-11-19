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
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {NgSelectModule} from "@ng-select/ng-select";
import {of, throwError} from "rxjs";
import {CreateCandidateCertificationComponent} from "./create-candidate-certification.component";
import {CandidateCertificationService} from "../../../../../services/candidate-certification.service";
import {CountryService} from "../../../../../services/country.service";
import {NgbActiveModal, NgbDatepickerModule} from "@ng-bootstrap/ng-bootstrap";
import {DatePickerComponent} from "../../../../util/date-picker/date-picker.component";
import {expect, vi} from 'vitest';

describe('CreateCandidateCertificationComponent', () => {
    let component: CreateCandidateCertificationComponent;
    let fixture: ComponentFixture<CreateCandidateCertificationComponent>;
    let candidateCertificationServiceSpy: MockedObject<CandidateCertificationService>;
    let countryServiceSpy: MockedObject<CountryService>;

    beforeEach(waitForAsync(() => {
        const candidateCertificationSpy = {
            create: vi.fn().mockName("CandidateCertificationService.create")
        };
        const countrySpy = {
            listCountries: vi.fn().mockName("CountryService.listCountries")
        };

        TestBed.configureTestingModule({
            declarations: [CreateCandidateCertificationComponent, DatePickerComponent],
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule, NgbDatepickerModule],
            providers: [
                NgbActiveModal,
                { provide: CandidateCertificationService, useValue: candidateCertificationSpy },
                { provide: CountryService, useValue: countrySpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        candidateCertificationServiceSpy = TestBed.inject(CandidateCertificationService) as MockedObject<CandidateCertificationService>;
        countryServiceSpy = TestBed.inject(CountryService) as MockedObject<CountryService>;
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(CreateCandidateCertificationComponent);
        component = fixture.componentInstance;
        countryServiceSpy.listCountries.mockReturnValue(of([]));
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize the form', () => {
        expect(component.candidateForm).toBeDefined();
        expect(component.candidateForm.controls['name']).toBeDefined();
        expect(component.candidateForm.controls['institution']).toBeDefined();
        expect(component.candidateForm.controls['dateCompleted']).toBeDefined();
    });

    it('form should be invalid when empty', () => {
        expect(component.candidateForm.valid).toBeFalsy();
    });

    it('form should be valid when filled', () => {
        component.candidateForm.controls['name'].setValue('Course Name');
        component.candidateForm.controls['institution'].setValue('Institution');
        component.candidateForm.controls['dateCompleted'].setValue('2024-05-29');
        expect(component.candidateForm.valid).toBeTruthy();
    });

    it('should call the create method on the service when form is valid and submitted', () => {
        const mockCertification = { id: 1, name: 'Course Name', institution: 'Institution', dateCompleted: '2024-05-29' };
        candidateCertificationServiceSpy.create.mockReturnValue(of(mockCertification));

        component.candidateId = 1;
        component.candidateForm.controls['name'].setValue('Course Name');
        component.candidateForm.controls['institution'].setValue('Institution');
        component.candidateForm.controls['dateCompleted'].setValue('2024-05-29');

        component.onSave();

        expect(vi.mocked(candidateCertificationServiceSpy.create).mock.calls.length).toBe(1);
    });

    it('should handle errors on form submission', () => {
        const mockError = 'An error occurred';
        candidateCertificationServiceSpy.create.mockReturnValue(throwError(mockError));

        component.candidateId = 1;
        component.candidateForm.controls['name'].setValue('Course Name');
        component.candidateForm.controls['institution'].setValue('Institution');
        component.candidateForm.controls['dateCompleted'].setValue('2024-05-29');

        component.onSave();

        expect(component.error).toBe(mockError);
        expect(vi.mocked(candidateCertificationServiceSpy.create).mock.calls.length).toBe(1);
    });
});

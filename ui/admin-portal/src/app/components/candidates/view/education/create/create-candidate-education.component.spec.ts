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
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CreateCandidateEducationComponent} from "./create-candidate-education.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CountryService} from "../../../../../services/country.service";
import {EducationMajorService} from "../../../../../services/education-major.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateEducationService, CreateCandidateEducationRequest} from "../../../../../services/candidate-education.service";
import {of, throwError} from "rxjs";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {expect, vi} from 'vitest';

describe('CreateCandidateEducationComponent', () => {
    let component: CreateCandidateEducationComponent;
    let fixture: ComponentFixture<CreateCandidateEducationComponent>;
    let mockCandidateEducationService: MockedObject<CandidateEducationService>;
    let mockCountryService: MockedObject<CountryService>;
    let mockEducationMajorService: MockedObject<EducationMajorService>;
    let mockActiveModal: MockedObject<NgbActiveModal>;

    beforeEach(async () => {
        const candidateEducationServiceSpy = {
            create: vi.fn().mockName("CandidateEducationService.create")
        };
        const countryServiceSpy = {
            listCountries: vi.fn().mockName("CountryService.listCountries")
        };
        const educationMajorServiceSpy = {
            listMajors: vi.fn().mockName("EducationMajorService.listMajors")
        };
        const activeModalSpy = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };

        await TestBed.configureTestingModule({
            declarations: [CreateCandidateEducationComponent],
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule],
            providers: [
                { provide: CandidateEducationService, useValue: candidateEducationServiceSpy },
                { provide: CountryService, useValue: countryServiceSpy },
                { provide: EducationMajorService, useValue: educationMajorServiceSpy },
                { provide: NgbActiveModal, useValue: activeModalSpy },
                UntypedFormBuilder,
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        })
            .compileComponents();

        mockCandidateEducationService = TestBed.inject(CandidateEducationService) as MockedObject<CandidateEducationService>;
        mockCountryService = TestBed.inject(CountryService) as MockedObject<CountryService>;
        mockEducationMajorService = TestBed.inject(EducationMajorService) as MockedObject<EducationMajorService>;
        mockActiveModal = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CreateCandidateEducationComponent);
        component = fixture.componentInstance;
        mockCountryService.listCountries.mockReturnValue(of([]));
        mockEducationMajorService.listMajors.mockReturnValue(of([]));
        component.candidateId = 1;
        fixture.detectChanges();
    });

    it('should ', () => {
        expect(component).toBeTruthy();
    });

    it('should save candidate education details', () => {
        const mockEducation = new MockCandidate().candidateEducations[0];


        mockCandidateEducationService.create.mockReturnValue(of(mockEducation));

        component.candidateForm.setValue({
            courseName: 'CS101',
            institution: 'MIT',
            countryId: 1,
            educationMajorId: 1,
            yearCompleted: 2010,
            lengthOfCourseYears: 4,
            educationType: 'Bachelor',
            incomplete: false
        });

        component.onSave();

        expect(mockCandidateEducationService.create).toHaveBeenCalledWith(expect.objectContaining({
            candidateId: component.candidateId,
            courseName: 'CS101',
            institution: 'MIT',
            countryId: 1,
            educationMajorId: 1,
            yearCompleted: 2010,
            lengthOfCourseYears: 4,
            educationType: 'Bachelor',
            incomplete: false
        } as unknown as CreateCandidateEducationRequest));

        expect(mockActiveModal.close).toHaveBeenCalledWith(mockEducation);
        expect(component.saving).toBe(false);
    });

    it('should handle error when saving candidate education details', () => {
        const error = 'Failed to save education details';
        mockCandidateEducationService.create.mockReturnValue(throwError(error));

        component.candidateForm.setValue({
            courseName: 'CS101',
            institution: 'MIT',
            countryId: 1,
            educationMajorId: 1,
            yearCompleted: 2010,
            lengthOfCourseYears: 4,
            educationType: 'Bachelor',
            incomplete: false
        });

        component.onSave();

        expect(mockCandidateEducationService.create).toHaveBeenCalled();
        expect(mockActiveModal.close).not.toHaveBeenCalled();
        expect(component.error).toBe(error);
        expect(component.saving).toBe(false);
    });
});

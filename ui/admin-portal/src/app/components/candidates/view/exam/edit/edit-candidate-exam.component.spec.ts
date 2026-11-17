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
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {EditCandidateExamComponent} from './edit-candidate-exam.component';
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {NgbActiveModal, NgbModalModule} from "@ng-bootstrap/ng-bootstrap";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {CandidateExamService} from "../../../../../services/candidate-exam.service";
import {of, throwError} from 'rxjs';
import {expect, vi} from 'vitest';

describe('EditCandidateExamComponent', () => {
    let component: EditCandidateExamComponent;
    let fixture: ComponentFixture<EditCandidateExamComponent>;
    let mockCandidateExamService: MockedObject<CandidateExamService>;
    let mockActiveModal: MockedObject<NgbActiveModal>;

    beforeEach(async () => {
        // Mock CandidateExamService
        mockCandidateExamService = ({
            update: vi.fn().mockName("CandidateExamService.update")
        } as unknown as typeof mockCandidateExamService);
        // Mock NgbActiveModal
        mockActiveModal = ({
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        } as unknown as typeof mockActiveModal);

        await TestBed.configureTestingModule({
            declarations: [EditCandidateExamComponent],
            imports: [NgbModalModule, ReactiveFormsModule, NgSelectModule],
            providers: [
                UntypedFormBuilder,
                { provide: NgbActiveModal, useValue: mockActiveModal },
                { provide: CandidateExamService, useValue: mockCandidateExamService },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(EditCandidateExamComponent);
        component = fixture.componentInstance;
        component.candidateExam = new MockCandidate().candidateExams[0];
        fixture.detectChanges();
    });

    it('should create the component', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form with candidate exam data', () => {
        expect(component.candidateForm).toBeTruthy();
        expect(component.candidateForm.controls['exam'].value).toEqual(component.candidateExam.exam);
        expect(component.candidateForm.controls['score'].value).toEqual(component.candidateExam.score);
        expect(component.candidateForm.controls['year'].value).toEqual(component.candidateExam.year);
        expect(component.candidateForm.controls['notes'].value).toBeNull();
    });

    it('should show other exam input field if exam is "Other"', () => {
        component.candidateForm.controls['exam'].setValue('Other');
        fixture.detectChanges();
        expect(component.isOtherExam).toBe(true);
    });

    it('should hide other exam input field if exam is not "Other"', () => {
        component.candidateForm.controls['exam'].setValue('IELTS');
        fixture.detectChanges();
        expect(component.isOtherExam).toBe(false);
    });

    it('should enable the score field if an exam is selected', () => {
        component.candidateForm.controls['exam'].setValue('IELTS');
        fixture.detectChanges();
        expect(component.hasSelectedExam).toBe(true);
    });

    it('should reflect when form is invalid', () => {
        component.candidateForm.controls['score'].setValue(null);
        fixture.detectChanges();
        expect(component.candidateForm.valid).toBe(false);
    });

    it('should call the update service when saving', () => {
        const mockRequest = {
            id: component.candidateExam.id,
            exam: component.candidateForm.value.exam,
            otherExam: component.candidateForm.value.otherExam,
            score: component.candidateForm.value.score,
            year: component.candidateForm.value.year,
            notes: component.candidateForm.value.notes
        };
        mockCandidateExamService.update.mockReturnValue(of(mockRequest));

        component.onSave();
        expect(mockCandidateExamService.update).toHaveBeenCalledWith(component.candidateExam.id, mockRequest);
        expect(component.saving).toBe(false);
    });

    it('should show error message if saving fails', () => {
        mockCandidateExamService.update.mockReturnValue(throwError('Error occurred'));

        component.onSave();
        expect(component.error).toBe('Error occurred');
        expect(component.saving).toBe(false);
    });

    it('should close the modal on successful save', () => {
        const mockRequest = {
            id: component.candidateExam.id,
            exam: component.candidateForm.value.exam,
            otherExam: component.candidateForm.value.otherExam,
            score: component.candidateForm.value.score,
            year: component.candidateForm.value.year,
            notes: component.candidateForm.value.notes
        };
        mockCandidateExamService.update.mockReturnValue(of(mockRequest));

        component.onSave();
        expect(mockActiveModal.close).toHaveBeenCalledWith(mockRequest);
    });

    it('should dismiss the modal', () => {
        component.dismiss();
        expect(mockActiveModal.dismiss).toHaveBeenCalledWith(false);
    });
});

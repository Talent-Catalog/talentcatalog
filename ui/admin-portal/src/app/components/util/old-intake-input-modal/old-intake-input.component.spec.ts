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
import {OldIntakeInputComponent} from "./old-intake-input.component";
import {CandidateService} from "../../../services/candidate.service";
import {CandidateNoteService} from "../../../services/candidate-note.service";
import {NgbActiveModal, NgbDatepickerModule, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {By} from "@angular/platform-browser";
import {of, throwError} from "rxjs";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {DatePickerComponent} from "../date-picker/date-picker.component";
import {expect, vi} from 'vitest';

describe('OldIntakeInputComponent', () => {
    let component: OldIntakeInputComponent;
    let fixture: ComponentFixture<OldIntakeInputComponent>;
    let candidateServiceSpy: MockedObject<CandidateService>;
    let candidateNoteServiceSpy: MockedObject<CandidateNoteService>;
    let activeModalSpy: MockedObject<NgbActiveModal>;

    beforeEach(async () => {
        const candidateSpy = {
            completeIntake: vi.fn().mockName("CandidateService.completeIntake")
        };
        const noteSpy = {
            create: vi.fn().mockName("CandidateNoteService.create")
        };
        const modalSpy = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };

        await TestBed.configureTestingModule({
            declarations: [OldIntakeInputComponent, DatePickerComponent],
            imports: [FormsModule, ReactiveFormsModule, NgbModule, NgbDatepickerModule],
            providers: [
                { provide: CandidateService, useValue: candidateSpy },
                { provide: CandidateNoteService, useValue: noteSpy },
                { provide: NgbActiveModal, useValue: modalSpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(OldIntakeInputComponent);
        component = fixture.componentInstance;
        candidateServiceSpy = TestBed.inject(CandidateService) as MockedObject<CandidateService>;
        candidateNoteServiceSpy = TestBed.inject(CandidateNoteService) as MockedObject<CandidateNoteService>;
        activeModalSpy = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form on ngOnInit', () => {
        component.ngOnInit();
        expect(component.form).toBeDefined();
        expect(component.form.controls.oldIntakeCompletedDate).toBeDefined();
        expect(component.form.controls.oldIntakeCompletedBy).toBeDefined();
    });

    it('should display error message when error is set', () => {
        component.error = 'Some error';
        fixture.detectChanges();
        const errorElement = fixture.debugElement.query(By.css('.alert-danger'));
        expect(errorElement).toBeTruthy();
        expect(errorElement.nativeElement.textContent).toContain('Some error');
    });

    it('should call candidateService.completeIntake and create note on save', () => {
        component.candidate = new MockCandidate();
        component.fullIntake = true;
        component.form.controls.oldIntakeCompletedDate.setValue('2024-01-01');
        component.form.controls.oldIntakeCompletedBy.setValue('Tester');
        candidateServiceSpy.completeIntake.mockReturnValue(of(component.candidate));
        candidateNoteServiceSpy.create.mockReturnValue(of({} as any));

        component.onSave();

        expect(candidateServiceSpy.completeIntake).toHaveBeenCalledWith(1, {
            completedDate: '2024-01-01',
            fullIntake: true
        });
        expect(candidateNoteServiceSpy.create).toHaveBeenCalledWith({
            candidateId: 1,
            title: 'Original intake data entered: Full Intake conducted on 2024-01-01 by Tester.',
            comment: 'See details below on who/when this data was entered into the TC. Can find original document in candidates Google drive.'
        });
    });

    it('should set error and stop saving if completeIntake fails', () => {
        component.candidate = { id: '123' } as any;
        candidateServiceSpy.completeIntake.mockReturnValue(throwError('Error'));

        component.onSave();

        expect(component.error).toBe('Error');
        expect(component.saving).toBe(false);
    });

    it('should set error and stop saving if createNote fails', () => {
        component.candidate = { id: '123' } as any;
        candidateServiceSpy.completeIntake.mockReturnValue(of(component.candidate));
        candidateNoteServiceSpy.create.mockReturnValue(throwError('Error'));

        component.onSave();

        expect(component.error).toBe('Error');
        expect(component.saving).toBe(false);
    });

    it('should close the modal on successful save', () => {
        component.candidate = { id: '123' } as any;
        candidateServiceSpy.completeIntake.mockReturnValue(of(component.candidate));
        candidateNoteServiceSpy.create.mockReturnValue(of({} as any));

        component.onSave();

        expect(activeModalSpy.close).toHaveBeenCalledWith(component.candidate);
    });

    it('should dismiss the modal on dismiss', () => {
        component.dismiss();
        expect(activeModalSpy.dismiss).toHaveBeenCalledWith(false);
    });
});

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
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {EditCandidateDestinationsComponent} from './edit-candidate-destinations.component';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {of, throwError} from "rxjs";
import {CandidateDestinationService} from "../../../../../../services/candidate-destination.service";
import {MockCandidate} from "../../../../../../MockData/MockCandidate";
import {expect, vi} from 'vitest';

describe('EditCandidateDestinationsComponent', () => {
    let component: EditCandidateDestinationsComponent;
    let fixture: ComponentFixture<EditCandidateDestinationsComponent>;
    let candidateDestinationServiceSpy: MockedObject<CandidateDestinationService>;
    let activeModalSpy: MockedObject<NgbActiveModal>;
    const mockCandidate = new MockCandidate();
    const mockCandidateDestination = mockCandidate.candidateDestinations[0];

    beforeEach(waitForAsync(() => {
        const candidateDestinationSpy = {
            update: vi.fn().mockName("CandidateDestinationService.update")
        };
        const activeModalMock = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };

        TestBed.configureTestingModule({
            declarations: [EditCandidateDestinationsComponent],
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule],
            providers: [
                { provide: NgbActiveModal, useValue: activeModalMock },
                { provide: CandidateDestinationService, useValue: candidateDestinationSpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        candidateDestinationServiceSpy = TestBed.inject(CandidateDestinationService) as MockedObject<CandidateDestinationService>;
        activeModalSpy = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(EditCandidateDestinationsComponent);
        component = fixture.componentInstance;
        component.candidateDestination = mockCandidateDestination;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize the form with candidateDestination data', () => {
        expect(component.form).toBeDefined();
        expect(component.form.controls['interest'].value).toBe('Yes');
        expect(component.form.controls['notes'].value).toBe('I like this country.');
    });

    it('form should be valid when filled correctly', () => {
        expect(component.form.valid).toBeTruthy();
    });

    it('should call the update method on the service when form is valid and submitted', () => {
        candidateDestinationServiceSpy.update.mockReturnValue(of(mockCandidateDestination));

        component.onSave();

        expect(vi.mocked(candidateDestinationServiceSpy.update).mock.calls.length).toBe(1);
        expect(vi.mocked(activeModalSpy.close).mock.calls.length).toBe(1);
    });

    it('should handle errors on form submission', () => {
        const mockError = 'An error occurred';
        candidateDestinationServiceSpy.update.mockReturnValue(throwError(mockError));

        component.onSave();

        expect(component.error).toBe(mockError);
        expect(vi.mocked(candidateDestinationServiceSpy.update).mock.calls.length).toBe(1);
        expect(vi.mocked(activeModalSpy.close).mock.calls.length).toBe(0);
    });

    it('should dismiss the modal without saving changes', () => {
        component.dismiss();
        expect(vi.mocked(activeModalSpy.dismiss).mock.calls.length).toBe(1);
    });
});

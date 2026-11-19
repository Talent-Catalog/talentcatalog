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
import {EditCandidateMediaWillingnessComponent} from "./edit-candidate-media-willingness.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../../services/candidate.service";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {of, throwError} from "rxjs";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {expect, vi} from 'vitest';

describe('EditCandidateMediaWillingnessComponent', () => {
    let component: EditCandidateMediaWillingnessComponent;
    let fixture: ComponentFixture<EditCandidateMediaWillingnessComponent>;
    let mockActiveModal: MockedObject<NgbActiveModal>;
    let mockCandidateService: MockedObject<CandidateService>;
    let fb: UntypedFormBuilder;
    const mockCandidate = new MockCandidate();
    beforeEach(async () => {
        const candidateServiceSpy = {
            get: vi.fn().mockName("CandidateService.get"),
            updateMedia: vi.fn().mockName("CandidateService.updateMedia")
        };
        const activeModalSpy = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };

        await TestBed.configureTestingModule({
            declarations: [EditCandidateMediaWillingnessComponent],
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule],
            providers: [
                UntypedFormBuilder,
                { provide: NgbActiveModal, useValue: activeModalSpy },
                { provide: CandidateService, useValue: candidateServiceSpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        })
            .compileComponents();

        fb = TestBed.inject(UntypedFormBuilder) as MockedObject<UntypedFormBuilder>;
        mockActiveModal = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
        mockCandidateService = TestBed.inject(CandidateService) as MockedObject<CandidateService>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(EditCandidateMediaWillingnessComponent);
        component = fixture.componentInstance;

        mockCandidateService.get.mockReturnValue(of(mockCandidate));
        component.candidateId = 1;
        fixture.detectChanges();
    });

    it('should create', () => {
        mockCandidateService.get.mockReturnValue(of(mockCandidate));
        fixture.detectChanges();
        expect(component).toBeTruthy();
    });

    it('should initialize form with candidate media willingness', () => {
        mockCandidateService.get.mockReturnValue(of(mockCandidate));

        fixture.detectChanges();

        expect(component.candidateForm).toBeDefined();
        expect(component.candidateForm.get('mediaWillingness').value).toEqual('Yes, open to media');
    });

    it('should call updateMedia on save and close the modal with updated candidate', () => {
        mockCandidateService.get.mockReturnValue(of(mockCandidate));
        mockCandidateService.updateMedia.mockReturnValue(of(mockCandidate));

        fixture.detectChanges();

        component.candidateForm.get('mediaWillingness').setValue('No');
        component.onSave();

        expect(mockCandidateService.updateMedia).toHaveBeenCalledWith(mockCandidate.id, { mediaWillingness: 'No' });
        expect(mockActiveModal.close).toHaveBeenCalledWith(mockCandidate);
    });

    it('should handle errors on save', () => {
        const errorResponse = { error: 'error message' };
        mockCandidateService.get.mockReturnValue(of(mockCandidate));
        mockCandidateService.updateMedia.mockReturnValue(throwError(errorResponse));

        fixture.detectChanges();

        component.candidateForm.get('mediaWillingness').setValue('No');
        component.onSave();

        expect(mockCandidateService.updateMedia).toHaveBeenCalledWith(1, { mediaWillingness: 'No' });
        expect(component.error).toEqual(errorResponse);
        expect(mockActiveModal.close).not.toHaveBeenCalled();
    });

    it('should dismiss the modal', () => {
        component.dismiss();
        expect(mockActiveModal.dismiss).toHaveBeenCalledWith(false);
    });
});

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
import {EditCandidateSpecialLinksComponent} from "./edit-candidate-special-links.component";
import {CandidateService} from "../../../../../services/candidate.service";
import {EnvService} from "../../../../../services/env.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {Candidate} from "../../../../../model/candidate";
import {of, throwError} from "rxjs";
import {expect, vi} from 'vitest';

describe('EditCandidateSpecialLinksComponent', () => {
    let component: EditCandidateSpecialLinksComponent;
    let fixture: ComponentFixture<EditCandidateSpecialLinksComponent>;
    let candidateService: MockedObject<CandidateService>;
    let envService: MockedObject<EnvService>;
    let activeModal: MockedObject<NgbActiveModal>;

    const mockCandidate = new MockCandidate();
    beforeEach(async () => {
        const candidateServiceSpy = {
            get: vi.fn().mockName("CandidateService.get"),
            updateLinks: vi.fn().mockName("CandidateService.updateLinks")
        };
        const envServiceSpy = {
            sfLightningUrl: 'https://salesforce.com'
        };
        const activeModalSpy = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };

        await TestBed.configureTestingModule({
            declarations: [EditCandidateSpecialLinksComponent],
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule],
            providers: [
                UntypedFormBuilder,
                { provide: CandidateService, useValue: candidateServiceSpy },
                { provide: EnvService, useValue: envServiceSpy },
                { provide: NgbActiveModal, useValue: activeModalSpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        candidateService = TestBed.inject(CandidateService) as MockedObject<CandidateService>;
        envService = TestBed.inject(EnvService) as MockedObject<EnvService>;
        activeModal = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(EditCandidateSpecialLinksComponent);
        component = fixture.componentInstance;
        component.candidateId = 1;
        candidateService.get.mockReturnValue(of(mockCandidate));
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize the form with the correct candidate data', fakeAsync(() => {
        const candidate: Candidate = mockCandidate;

        candidateService.get.mockReturnValue(of(candidate));

        component.ngOnInit();
        tick(); // simulate async

        expect(component.candidateForm).toBeDefined();
        expect(component.candidateForm.value).toEqual({
            linkedInLink: candidate.linkedInLink,
            folderlink: candidate.folderlink,
            sflink: candidate.sflink,
            videolink: candidate.videolink
        });
        expect(component.loading).toBe(false);
    }));



    it('should call updateLinks when onSave is called', fakeAsync(() => {
        const candidate: Candidate = mockCandidate;

        candidateService.get.mockReturnValue(of(candidate));
        candidateService.updateLinks.mockReturnValue(of(candidate));

        component.ngOnInit();
        tick(); // simulate async

        component.onSave();

        tick(); // simulate async
        expect(candidateService.updateLinks).toHaveBeenCalledWith(component.candidateId, component.candidateForm.value);
        expect(component.saving).toBe(false);
        expect(activeModal.close).toHaveBeenCalledWith(candidate);
    }));

    it('should display an error message if updating links fails', fakeAsync(() => {
        const candidate: Candidate = mockCandidate;
        const errorResponse = 'Error updating links';

        candidateService.get.mockReturnValue(of(candidate));
        candidateService.updateLinks.mockReturnValue(throwError(errorResponse));

        component.ngOnInit();
        tick(); // simulate async

        component.onSave();

        tick(); // simulate async
        expect(candidateService.updateLinks).toHaveBeenCalledWith(component.candidateId, component.candidateForm.value);
        expect(component.saving).toBe(false);
        expect(component.error).toBe(errorResponse);
    }));

    it('should dismiss the modal when dismiss is called', () => {
        component.dismiss();
        expect(activeModal.dismiss).toHaveBeenCalledWith(false);
    });
});

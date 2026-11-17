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
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';

import {CandidateVisaJobComponent} from './candidate-visa-job.component';
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {MockCandidate} from "../../../../../../MockData/MockCandidate";
import {CandidateVisaJobService} from "../../../../../../services/candidate-visa-job.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../../../../services/authorization.service";
import {CandidateService} from "../../../../../../services/candidate.service";
import {mockCandidateIntakeData} from "../../candidate-intake-tab/candidate-intake-tab.component.spec";
import {MockCandidateVisaJobCheck} from "../../../../../../MockData/MockCandidateVisaCheck";
import {MockCandidateVisa} from "../../../../../../MockData/MockCandidateVisa";
import {MockJob} from "../../../../../../MockData/MockJob";
import {CandidateVisaJobCheck} from "../../../../../../model/candidate";
import {of} from "rxjs";
import {Job} from "../../../../../../model/job";
import {RouterTestingModule} from "@angular/router/testing";
import {expect, vi} from 'vitest';

describe('CandidateVisaJobComponent', () => {
    let component: CandidateVisaJobComponent;
    let fixture: ComponentFixture<CandidateVisaJobComponent>;
    let candidateVisaJobServiceMock: MockedObject<CandidateVisaJobService>;
    let modalServiceMock: MockedObject<NgbModal>;
    let authServiceMock: MockedObject<AuthorizationService>;
    const mockCandidate = new MockCandidate();

    beforeEach(async () => {
        candidateVisaJobServiceMock = ({
            create: vi.fn().mockName("CandidateService.create"),
            delete: vi.fn().mockName("CandidateService.delete"),
            get: vi.fn().mockName("CandidateService.get")
        } as unknown as typeof candidateVisaJobServiceMock);
        modalServiceMock = ({
            open: vi.fn().mockName("NgbModal.open")
        } as unknown as typeof modalServiceMock);
        authServiceMock = ({
            isSystemAdminOnly: vi.fn().mockName("AuthorizationService.isSystemAdminOnly"),
            isEditableCandidate: vi.fn().mockName("AuthorizationService.isEditableCandidate")
        } as unknown as typeof authServiceMock);
        await TestBed.configureTestingModule({
            imports: [FormsModule, ReactiveFormsModule, RouterTestingModule],
            declarations: [CandidateVisaJobComponent],
            providers: [
                UntypedFormBuilder,
                { provide: CandidateVisaJobService, useValue: candidateVisaJobServiceMock },
                { provide: NgbModal, useValue: modalServiceMock },
                { provide: AuthorizationService, useValue: authServiceMock },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting(),
            ],
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CandidateVisaJobComponent);
        component = fixture.componentInstance;
        component.candidate = new MockCandidate();
        component.candidateIntakeData = mockCandidateIntakeData;
        component.visaCheckRecord = { ...MockCandidateVisa }; // Use a fresh object
        component.selectedJob = { ...MockCandidateVisaJobCheck }; // Use a fresh object


        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should emit the first job by default on init', () => {
        vi.spyOn(component.selectedJobChange, 'emit').mockReturnValue(undefined);
        component.ngOnInit();
        expect(component.selectedJobChange.emit).toHaveBeenCalledWith(component.visaCheckRecord.candidateVisaJobChecks[0]);
    });

    it('should create a visa job check when a job is selected', fakeAsync(() => {
        const job: Job = MockJob;
        const newVisaJobCheck: CandidateVisaJobCheck = { id: 1, jobOpp: job } as CandidateVisaJobCheck;

        // Mock the modal result
        const modalRef = {
            result: Promise.resolve(job),
            componentInstance: {}
        };
        modalServiceMock.open.mockReturnValue(modalRef as any);

        // Mock the create service call
        candidateVisaJobServiceMock.create.mockReturnValue(of(newVisaJobCheck));

        vi.spyOn(component.selectedJobChange, 'emit').mockReturnValue(undefined);

        // Simulate button click to add a job
        component.addJob();
        tick();

        // Before asserting, explicitly patch the form with jobIndex, isolating it within this test
        component.form.patchValue({ jobIndex: 1 });

        fixture.detectChanges();

        // Verify the service call and the update to visaChecks
        expect(candidateVisaJobServiceMock.create).toHaveBeenCalledWith(1, { jobOppId: job.id });
        expect(component.form.value.jobIndex).toBe(1); // Ensuring jobIndex is 1 here
        expect(component.selectedJobChange.emit).toHaveBeenCalledWith(newVisaJobCheck);
    }));


});

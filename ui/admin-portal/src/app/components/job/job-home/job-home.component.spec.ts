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
import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from '@angular/core/testing';
import {JobHomeComponent} from './job-home.component';
import {CandidateOpportunityService} from '../../../services/candidate-opportunity.service';
import {JobService} from '../../../services/job.service';
import {of, throwError} from 'rxjs';
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {JobsWithDetailComponent} from "../jobs-with-detail/jobs-with-detail.component";
import {ActivatedRoute, Router} from "@angular/router";
import {JobsComponent} from "../jobs/jobs.component";
import {NgSelectModule} from "@ng-select/ng-select";
import {SortedByComponent} from "../../util/sort/sorted-by.component";
import {ChatReadStatusComponent} from "../../chat/chat-read-status/chat-read-status.component";
import {CandidateService} from "../../../services/candidate.service";
import {expect, vi} from 'vitest';

describe('JobHomeComponent', () => {
    let component: JobHomeComponent;
    let fixture: ComponentFixture<JobHomeComponent>;
    let candidateOpportunityService: MockedObject<CandidateOpportunityService>;
    let jobService: MockedObject<JobService>;
    let candidateService: MockedObject<CandidateService>;

    beforeEach(waitForAsync(() => {
        // Creating spy objects for the services
        const candidateOpportunityServiceSpy = {
            checkUnreadChats: vi.fn().mockName("CandidateOpportunityService.checkUnreadChats")
        };
        const jobServiceSpy = {
            checkUnreadChats: vi.fn().mockName("JobService.checkUnreadChats"),
            searchPaged: vi.fn().mockName("JobService.searchPaged")
        };
        const candidateServiceSpy = {
            checkUnreadChats: vi.fn().mockName("CandidateService.checkUnreadChats")
        };
        TestBed.configureTestingModule({
            declarations: [JobHomeComponent, SortedByComponent, ChatReadStatusComponent, JobsWithDetailComponent, JobsComponent],
            imports: [
                ReactiveFormsModule,
                NgbModule,
                NgSelectModule
            ],
            providers: [
                { provide: CandidateOpportunityService, useValue: candidateOpportunityServiceSpy },
                { provide: JobService, useValue: jobServiceSpy },
                { provide: CandidateService, useValue: candidateServiceSpy },
                { provide: UntypedFormBuilder, useClass: UntypedFormBuilder },
                { provide: ActivatedRoute, useValue: { queryParams: of({}) } },
                { provide: Router, useValue: { navigateByUrl: vi.fn().mockName('navigateByUrl') } },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        })
            .compileComponents();

        // Getting references to the spy objects
        candidateOpportunityService = TestBed.inject(CandidateOpportunityService) as MockedObject<CandidateOpportunityService>;
        jobService = TestBed.inject(JobService) as MockedObject<JobService>;
        candidateService = TestBed.inject(CandidateService) as MockedObject<CandidateService>;

        // Mocking return values for the service methods
        candidateOpportunityService.checkUnreadChats.mockReturnValue(of({ numberUnreadChats: 0 }));
        jobService.checkUnreadChats.mockReturnValue(of({ numberUnreadChats: 0 }));
        jobServiceSpy.searchPaged.mockReturnValue(of());
        candidateService.checkUnreadChats.mockReturnValue(of({ numberUnreadChats: 0 }));
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(JobHomeComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
    it('should call checkUnreadChats methods of both services on initialization', () => {
        // Expectations to ensure service methods were called on initialization
        expect(candidateOpportunityService.checkUnreadChats).toHaveBeenCalled();
        expect(jobService.checkUnreadChats).toHaveBeenCalled();
        expect(candidateService.checkUnreadChats).toHaveBeenCalled();
    });

    it('should load chat read statuses on initialization', () => {
        // Setting up mock data for the services
        const mockJobChatUserInfo = { numberUnreadChats: 1 }; // Mock data for job chat user info
        const mockOpportunityRequest = expect.any(Object); // Mock search opportunity request

        // Mocking the return values of the service methods
        candidateOpportunityService.checkUnreadChats.mockReturnValue(of(mockJobChatUserInfo));
        jobService.checkUnreadChats.mockReturnValue(of(mockJobChatUserInfo));
        candidateService.checkUnreadChats.mockReturnValue(of(mockJobChatUserInfo));
        // Triggering ngOnInit
        component.ngOnInit();

        // Expectations to ensure the service methods were called with correct arguments
        expect(candidateOpportunityService.checkUnreadChats).toHaveBeenCalledWith(mockOpportunityRequest);
        expect(jobService.checkUnreadChats).toHaveBeenCalledWith(mockOpportunityRequest);
        expect(candidateService.checkUnreadChats).toHaveBeenCalledWith();

        // Expectations to ensure the chat read statuses were loaded correctly
        expect(component.jobCreatorChatsRead$.getValue()).toBe(false);
        expect(component.sourcePartnerChatsRead$.getValue()).toBe(false);
        expect(component.partnerJobChatsRead$.getValue()).toBe(false);
        expect(component.starredJobChatsRead$.getValue()).toBe(false);
        expect(component.candidatesWithChatRead$.getValue()).toBe(false);
    });
    it('should handle errors when checkUnreadChats methods of services throw errors', () => {
        // Mocking service methods to throw errors
        candidateOpportunityService.checkUnreadChats.mockReturnValue(throwError('Test error'));
        jobService.checkUnreadChats.mockReturnValue(throwError('Test error'));
        candidateService.checkUnreadChats.mockReturnValue(throwError('Test error'));

        // Trigger ngOnInit
        component.ngOnInit();

        // Expectations to ensure error handling
        expect(component.jobCreatorChatsRead$.getValue()).toBe(true); // Assuming error handling sets the value to true
        expect(component.sourcePartnerChatsRead$.getValue()).toBe(true); // Assuming error handling sets the value to true
        expect(component.partnerJobChatsRead$.getValue()).toBe(true); // Assuming error handling sets the value to true
        expect(component.starredJobChatsRead$.getValue()).toBe(true); // Assuming error handling sets the value to true
        expect(component.candidatesWithChatRead$.getValue()).toBe(true); // Assuming error handling sets the value to true
    });
    it('should render in acceptable time', fakeAsync(() => {
        // Mock return values for the service methods
        candidateOpportunityService.checkUnreadChats.mockReturnValue(of({ numberUnreadChats: 0 }));
        jobService.checkUnreadChats.mockReturnValue(of({ numberUnreadChats: 0 }));
        candidateService.checkUnreadChats.mockReturnValue(of({ numberUnreadChats: 0 }));

        // Start timing
        const startTime = performance.now();

        // Trigger ngOnInit
        component.ngOnInit();
        tick();

        // End timing
        const endTime = performance.now();
        const renderTime = endTime - startTime;

        // Assert that rendering time is within acceptable limits (e.g., less than 1000ms)
        expect(renderTime).toBeLessThan(1000); // Adjust as needed
    }));
});

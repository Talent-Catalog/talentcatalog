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
import {JobsWithDetailComponent} from './jobs-with-detail.component';
import {of, Subject, throwError} from 'rxjs';
import {Job} from '../../../model/job';
import {JobService} from '../../../services/job.service';
import {AuthenticationService} from '../../../services/authentication.service';
import {JobsComponent} from '../jobs/jobs.component';
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {ReactiveFormsModule} from "@angular/forms";
import {NgbPaginationModule} from "@ng-bootstrap/ng-bootstrap";
import {NgSelectModule} from "@ng-select/ng-select";
import {Router} from "@angular/router";
import {SortedByComponent} from "../../util/sort/sorted-by.component";
import {ChatReadStatusComponent} from "../../chat/chat-read-status/chat-read-status.component";
import {MockJob} from "../../../MockData/MockJob";
import {MockUser} from "../../../MockData/MockUser";
import {expect, vi} from 'vitest';

describe('JobsWithDetailComponent', () => {
    let component: JobsWithDetailComponent;
    let fixture: ComponentFixture<JobsWithDetailComponent>;
    let jobService: MockedObject<JobService>;
    let authService: MockedObject<AuthenticationService>;
    let jobWithUser = MockJob;
    let mockedUser = new MockUser();
    beforeEach(waitForAsync(() => {
        jobWithUser.starringUsers[0].id = 1; //Set the first Starring User index to 1
        const jobServiceSpy = {
            checkUnreadChats: vi.fn().mockName("JobService.checkUnreadChats"),
            updateStarred: vi.fn().mockName("JobService.updateStarred"),
            searchPaged: vi.fn().mockName("JobService.searchPaged"),
            get: vi.fn().mockName("JobService.get")
        };
        const authServiceSpy = {
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser"),
            currentUser: { id: 1 }, loggedInUser$: new Subject<any>()
        };
        TestBed.configureTestingModule({
            declarations: [SortedByComponent, ChatReadStatusComponent, JobsWithDetailComponent, JobsComponent],
            imports: [
                ReactiveFormsModule,
                NgbPaginationModule,
                NgSelectModule
            ],
            providers: [
                { provide: JobService, useValue: jobServiceSpy },
                { provide: AuthenticationService, useValue: authServiceSpy },
                { provide: Router, useValue: { navigateByUrl: vi.fn().mockName('navigateByUrl') } },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting(),
            ]
        })
            .compileComponents();

        jobService = TestBed.inject(JobService) as MockedObject<JobService>;
        authService = TestBed.inject(AuthenticationService) as MockedObject<AuthenticationService>;

        // Mocking return values for the service methods
        jobService.checkUnreadChats.mockReturnValue(of({ numberUnreadChats: 0 }));
        jobService.updateStarred.mockReturnValue(throwError("error"));
        jobService.get.mockReturnValue(of(jobWithUser));
        jobServiceSpy.searchPaged.mockReturnValue(of());
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(JobsWithDetailComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('Ensure that selectedJob, error, and loading properties are initialized correctly', () => {
        expect(component.selectedJob).toBeUndefined();
        expect(component.error).toBeUndefined();
        expect(component.loading).toBeFalsy();
    });

    it('should select a job', fakeAsync(() => {
        component.onJobSelected(jobWithUser);
        tick();
        expect(component.selectedJob).toEqual(jobWithUser);
    }));

    it('should refresh jobs component when job is updated', () => {
        const jobsComponentSpy = {
            search: vi.fn().mockName("JobsComponent.search")
        };
        component.jobsComponent = (jobsComponentSpy as unknown as typeof component.jobsComponent);

        component.onJobUpdated({} as Job);
        expect(jobsComponentSpy.search).toHaveBeenCalled();
    });

    it('should correctly identify whether the authenticated user has starred the job', () => {
        // Mock the authentication service to return a mock user ID
        authService.getLoggedInUser.mockReturnValue(mockedUser);
        // Test when the authenticated user has starred the job
        component.selectedJob = jobWithUser;
        let starred = component.isStarred();
        expect(starred).toBeTruthy();

        jobWithUser.starringUsers[0].id = 2;
        // Test when the authenticated user has not starred the job
        component.selectedJob = jobWithUser;
        starred = component.isStarred();
        expect(starred).toBeFalsy();
    });

    it('should handle error when toggling starred status', fakeAsync(() => {
        const error = 'Error toggling starred status';
        jobService.updateStarred.mockReturnValue(throwError(error));
        component.selectedJob = jobWithUser;
        component.doToggleStarred();
        tick(); // Wait for observable to resolve
        expect(component.loading).toBeFalsy();
        expect(component.error).toEqual(error);
    }));
});

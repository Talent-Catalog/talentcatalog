/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from '@angular/core/testing';
import { JobHomeComponent } from './job-home.component';
import { CandidateOpportunityService } from '../../../services/candidate-opportunity.service';
import { JobService } from '../../../services/job.service';
import {of, throwError} from 'rxjs';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {UntypedFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {JobsWithDetailComponent} from "../jobs-with-detail/jobs-with-detail.component";
import { Router} from "@angular/router";
import {JobsComponent} from "../jobs/jobs.component";
import {NgSelectModule} from "@ng-select/ng-select";
import {SortedByComponent} from "../../util/sort/sorted-by.component";
import {ChatReadStatusComponent} from "../../chat/chat-read-status/chat-read-status.component";
import {CandidateService} from "../../../services/candidate.service";

describe('JobHomeComponent', () => {
  let component: JobHomeComponent;
  let fixture: ComponentFixture<JobHomeComponent>;
  let candidateOpportunityService: jasmine.SpyObj<CandidateOpportunityService>;
  let jobService: jasmine.SpyObj<JobService>;
  let candidateService: jasmine.SpyObj<CandidateService>;

  beforeEach(waitForAsync(() => {
    // Creating spy objects for the services
    const candidateOpportunityServiceSpy = jasmine.createSpyObj('CandidateOpportunityService', ['checkUnreadChats']);
    const jobServiceSpy = jasmine.createSpyObj('JobService', ['checkUnreadChats','searchPaged']);
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['checkUnreadChats']);
     TestBed.configureTestingModule({
      declarations: [JobHomeComponent,SortedByComponent,ChatReadStatusComponent,JobsWithDetailComponent,JobsComponent],
      imports: [
        HttpClientTestingModule,
        ReactiveFormsModule,
        NgbModule,
        NgSelectModule
      ],
      providers: [
        { provide: CandidateOpportunityService, useValue: candidateOpportunityServiceSpy },
        { provide: JobService, useValue: jobServiceSpy },
        { provide: CandidateService, useValue: candidateServiceSpy },
        { provide: UntypedFormBuilder, useClass: UntypedFormBuilder },
        { provide: Router, useValue: { navigateByUrl: jasmine.createSpy('navigateByUrl') }}
      ]
    })
    .compileComponents();

    // Getting references to the spy objects
    candidateOpportunityService = TestBed.inject(CandidateOpportunityService) as jasmine.SpyObj<CandidateOpportunityService>;
    jobService = TestBed.inject(JobService) as jasmine.SpyObj<JobService>;
    candidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;

    // Mocking return values for the service methods
    candidateOpportunityService.checkUnreadChats.and.returnValue(of({ numberUnreadChats: 0 }));
    jobService.checkUnreadChats.and.returnValue(of({ numberUnreadChats: 0 }));
    jobServiceSpy.searchPaged.and.returnValue(of());
    candidateService.checkUnreadChats.and.returnValue(of({ numberUnreadChats: 0 }));
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
    const mockOpportunityRequest = jasmine.any(Object); // Mock search opportunity request

    // Mocking the return values of the service methods
    candidateOpportunityService.checkUnreadChats.and.returnValue(of(mockJobChatUserInfo));
    jobService.checkUnreadChats.and.returnValue(of(mockJobChatUserInfo));
    candidateService.checkUnreadChats.and.returnValue(of(mockJobChatUserInfo));
     // Triggering ngOnInit
    component.ngOnInit();

    // Expectations to ensure the service methods were called with correct arguments
    expect(candidateOpportunityService.checkUnreadChats).toHaveBeenCalledWith(mockOpportunityRequest);
    expect(jobService.checkUnreadChats).toHaveBeenCalledWith(mockOpportunityRequest);
    expect(candidateService.checkUnreadChats).toHaveBeenCalledWith();

    // Expectations to ensure the chat read statuses were loaded correctly
    expect(component.jobCreatorChatsRead$.getValue()).toBeFalse();
    expect(component.sourcePartnerChatsRead$.getValue()).toBeFalse();
    expect(component.partnerJobChatsRead$.getValue()).toBeFalse();
    expect(component.starredJobChatsRead$.getValue()).toBeFalse();
    expect(component.candidatesWithChatRead$.getValue()).toBeFalse();
  });
  it('should handle errors when checkUnreadChats methods of services throw errors', () => {
    // Mocking service methods to throw errors
    candidateOpportunityService.checkUnreadChats.and.returnValue(throwError('Test error'));
    jobService.checkUnreadChats.and.returnValue(throwError('Test error'));
    candidateService.checkUnreadChats.and.returnValue(throwError('Test error'));

    // Trigger ngOnInit
    component.ngOnInit();

    // Expectations to ensure error handling
    expect(component.jobCreatorChatsRead$.getValue()).toBeTrue(); // Assuming error handling sets the value to true
    expect(component.sourcePartnerChatsRead$.getValue()).toBeTrue(); // Assuming error handling sets the value to true
    expect(component.partnerJobChatsRead$.getValue()).toBeTrue(); // Assuming error handling sets the value to true
    expect(component.starredJobChatsRead$.getValue()).toBeTrue(); // Assuming error handling sets the value to true
    expect(component.candidatesWithChatRead$.getValue()).toBeTrue(); // Assuming error handling sets the value to true
  });
  it('should render in acceptable time', fakeAsync(() => {
    // Mock return values for the service methods
    candidateOpportunityService.checkUnreadChats.and.returnValue(of({ numberUnreadChats: 0 }));
    jobService.checkUnreadChats.and.returnValue(of({ numberUnreadChats: 0 }));
    candidateService.checkUnreadChats.and.returnValue(of({ numberUnreadChats: 0 }));

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

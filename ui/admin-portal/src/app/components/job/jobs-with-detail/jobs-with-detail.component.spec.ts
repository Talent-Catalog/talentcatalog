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

import { ComponentFixture, TestBed, waitForAsync, fakeAsync, tick } from '@angular/core/testing';
import { JobsWithDetailComponent } from './jobs-with-detail.component';
import {  of, throwError } from 'rxjs';
import { Job } from '../../../model/job';
import { JobService } from '../../../services/job.service';
import { AuthenticationService } from '../../../services/authentication.service';
import { JobsComponent } from '../jobs/jobs.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {NgbPaginationModule} from "@ng-bootstrap/ng-bootstrap";
import {NgSelectModule} from "@ng-select/ng-select";
import {Router} from "@angular/router";
import {SortedByComponent} from "../../util/sort/sorted-by.component";
import {ChatReadStatusComponent} from "../../chat/chat-read-status/chat-read-status.component";
import {MockJob} from "../../../MockData/MockJob";
import {MockUser} from "../../../MockData/MockUser";

describe('JobsWithDetailComponent', () => {
  let component: JobsWithDetailComponent;
  let fixture: ComponentFixture<JobsWithDetailComponent>;
  let jobService: jasmine.SpyObj<JobService>;
  let authService: jasmine.SpyObj<AuthenticationService>;
  let jobWithUser = MockJob;
  let mockedUser = new MockUser();
  beforeEach(waitForAsync(() => {
    jobWithUser.starringUsers[0].id=1; //Set the first Starring User index to 1
    const jobServiceSpy = jasmine.createSpyObj('JobService', ['checkUnreadChats','updateStarred','searchPaged']);
    const authServiceSpy = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser'], { currentUser: { id: 1 } });
    TestBed.configureTestingModule({
      declarations: [SortedByComponent,ChatReadStatusComponent,JobsWithDetailComponent,JobsComponent],
      imports: [
        HttpClientTestingModule,
        ReactiveFormsModule,
        NgbPaginationModule,
        NgSelectModule
      ],
      providers: [
        { provide: JobService, useValue: jobServiceSpy },
        { provide: AuthenticationService, useValue: authServiceSpy },
        { provide: Router, useValue: { navigateByUrl: jasmine.createSpy('navigateByUrl') } },
      ]
    })
    .compileComponents();

    jobService = TestBed.inject(JobService) as jasmine.SpyObj<JobService>;
    authService = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;

    // Mocking return values for the service methods
    jobService.checkUnreadChats.and.returnValue(of({ numberUnreadChats: 0 }));
    jobService.updateStarred.and.returnValue(throwError("error"));
    jobServiceSpy.searchPaged.and.returnValue(of());
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

  it('should select a job', () => {
    component.onJobSelected(jobWithUser);
    expect(component.selectedJob).toEqual(jobWithUser);
  });
  it('should refresh jobs component when job is updated', () => {
    const jobsComponentSpy = jasmine.createSpyObj('JobsComponent', ['search']);
    component.jobsComponent = jobsComponentSpy;

    component.onJobUpdated({} as Job);
    expect(jobsComponentSpy.search).toHaveBeenCalled();
  });
  it('should correctly identify whether the authenticated user has starred the job', () => {
    // Mock the authentication service to return a mock user ID
    authService.getLoggedInUser.and.returnValue(mockedUser);
    // Test when the authenticated user has starred the job
    component.selectedJob = jobWithUser;
    let starred = component.isStarred();
    expect(starred).toBeTruthy();

    jobWithUser.starringUsers[0].id=2;
    // Test when the authenticated user has not starred the job
    component.selectedJob = jobWithUser;
    starred = component.isStarred();
    expect(starred).toBeFalsy();
  });
  it('should handle error when toggling starred status', fakeAsync(() => {
    const error = 'Error toggling starred status';
    jobService.updateStarred.and.returnValue(throwError(error));
    component.selectedJob = jobWithUser;
    component.doToggleStarred();
    tick(); // Wait for observable to resolve
    expect(component.loading).toBeFalsy();
    expect(component.error).toEqual(error);
  }));
});

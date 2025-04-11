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

import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {ViewJobComponent} from './view-job.component';
import {NgbModal, NgbNavModule, NgbTooltipModule} from '@ng-bootstrap/ng-bootstrap';
import {AuthenticationService} from '../../../../services/authentication.service';
import {JobService} from '../../../../services/job.service';
import {SlackService} from '../../../../services/slack.service';
import {Router} from '@angular/router';
import {CommonModule, Location} from '@angular/common';
import {of} from 'rxjs';
import {Job} from '../../../../model/job';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {MockUser} from "../../../../MockData/MockUser";
import {MockJob} from "../../../../MockData/MockJob";
import {JobGeneralTabComponent} from "../tab/job-general-tab/job-general-tab.component";
import {ViewJobInfoComponent} from "../info/view-job-info/view-job-info.component";
import {ViewJobSummaryComponent} from "../summary/view-job-summary/view-job-summary.component";
import {ChatReadStatusComponent} from "../../../chat/chat-read-status/chat-read-status.component";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {RouterLinkStubDirective} from "../../../login/login.component.spec";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
// Mock isStarredByMe function
const isStarredByMe = (starringUsers: any[], authService: AuthenticationService) => {
  const loggedInUser = authService.getLoggedInUser();
  console.log(loggedInUser)
  return starringUsers.some(user => user.id === loggedInUser.id);
};

describe('ViewJobComponent', () => {
  let component: ViewJobComponent;
  let fixture: ComponentFixture<ViewJobComponent>;
  let mockAuthService: jasmine.SpyObj<AuthenticationService>;
  let mockJobService: jasmine.SpyObj<JobService>;
  let mockSlackService: jasmine.SpyObj<SlackService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockLocation: jasmine.SpyObj<Location>;

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
    mockJobService = jasmine.createSpyObj('JobService', ['updateStarred']);
    mockSlackService = jasmine.createSpyObj('SlackService', ['postJobFromId']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockLocation = jasmine.createSpyObj('Location', ['back']);

    await TestBed.configureTestingModule({
      imports:[HttpClientTestingModule,NgbNavModule,ReactiveFormsModule,CommonModule,NgbTooltipModule],
      declarations: [ViewJobComponent,AutosaveStatusComponent,RouterLinkStubDirective,JobGeneralTabComponent,ViewJobInfoComponent,ViewJobSummaryComponent,ChatReadStatusComponent],
      providers: [
        UntypedFormBuilder,
        { provide: NgbModal, useValue: {} },
        { provide: AuthenticationService, useValue: mockAuthService },
        { provide: JobService, useValue: mockJobService },
        { provide: SlackService, useValue: mockSlackService },
        { provide: Router, useValue: mockRouter },
        { provide: Location, useValue: mockLocation }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobComponent);
    component = fixture.componentInstance;
    component.job=MockJob;

    // Override the isStarredByMe method
    (window as any).isStarredByMe = isStarredByMe;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should toggle starred status of job when initially starred', fakeAsync(() => {
    // Arrange
    const loggedInUser = new MockUser();
    mockAuthService.getLoggedInUser.and.returnValue(loggedInUser);

    // Initial state where the job is starred by the user
    const job: Job = { ...MockJob, starringUsers: [loggedInUser] };
    component.job = job;

    // Toggled state where the job is no longer starred
    const updatedJob = { ...job, starringUsers: [] };
    mockJobService.updateStarred.and.returnValue(of(updatedJob));
    // Act
    component.doToggleStarred();
    tick();

    // Assert
    expect(mockJobService.updateStarred).toHaveBeenCalledWith(job.id, false);
    expect(component.job).toEqual(updatedJob);
    expect(component.loading).toBeFalsy();
  }));

  it('should toggle starred status of job when initially not starred', fakeAsync(() => {
    // Arrange
    const loggedInUser = new MockUser();
    mockAuthService.getLoggedInUser.and.returnValue(loggedInUser);

    // Initial state where the job is not starred by the user
    const job: Job = { ...MockJob, starringUsers: [] };
    component.job = job;

    // Toggled state where the job is now starred
    const updatedJob = { ...job, starringUsers: [loggedInUser] };
    mockJobService.updateStarred.and.returnValue(of(updatedJob));

    // Act
    component.doToggleStarred();
    tick();

    // Assert
    expect(mockJobService.updateStarred).toHaveBeenCalledWith(job.id, true);
    expect(component.job).toEqual(updatedJob);
    expect(component.loading).toBeFalsy();
  }));

});

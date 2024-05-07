import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import { ViewJobComponent } from './view-job.component';
import {NgbModal, NgbNavModule, NgbTooltipModule} from '@ng-bootstrap/ng-bootstrap';
import { AuthenticationService } from '../../../../services/authentication.service';
import { JobService } from '../../../../services/job.service';
import { SlackService } from '../../../../services/slack.service';
import { Router } from '@angular/router';
import {CommonModule, Location} from '@angular/common';
import {of, throwError} from 'rxjs';
import { Job } from '../../../../model/job';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {LocalStorageModule} from "angular-2-local-storage";
import {MockUser} from "../../../../MockData/MockUser";
import {MockJob} from "../../../../MockData/MockJob";
import {
  RouterLinkDirectiveStub
} from "../submission-list/view-job-submission-list/view-job-submission-list.component.spec";
import {JobGeneralTabComponent} from "../tab/job-general-tab/job-general-tab.component";
import {ViewJobInfoComponent} from "../info/view-job-info/view-job-info.component";
import {ViewJobSummaryComponent} from "../summary/view-job-summary/view-job-summary.component";
import {ChatReadStatusComponent} from "../../../chat/chat-read-status/chat-read-status.component";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";

fdescribe('ViewJobComponent', () => {
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
      imports:[HttpClientTestingModule,LocalStorageModule.forRoot({}),NgbNavModule,ReactiveFormsModule,CommonModule,NgbTooltipModule],
      declarations: [ViewJobComponent,RouterLinkDirectiveStub,JobGeneralTabComponent,ViewJobInfoComponent,ViewJobSummaryComponent,ChatReadStatusComponent],
      providers: [
        FormBuilder,
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
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle starred status of job', fakeAsync(() => {
    // Arrange
    const loggedInUser = new MockUser();
    mockAuthService.getLoggedInUser.and.returnValue(loggedInUser);
    const job: Job = MockJob;
    mockJobService.updateStarred.and.returnValue(of(job));
    spyOn(component.jobUpdated, 'emit');
    component.onJobUpdated(job);
    // Act
    component.doToggleStarred();
    // Assert
     expect(mockJobService.updateStarred).toHaveBeenCalledWith(1, false);
     expect(component.job).toEqual(job);
     expect(component.jobUpdated.emit).toHaveBeenCalledWith(job);
     expect(component.loading).toBeFalsy(); // Ensure loading indicator is turned off
  }));
  it('should handle error when updating starred status of job', fakeAsync(() => {
    // Arrange
    const loggedInUser = new MockUser();
    mockAuthService.getLoggedInUser.and.returnValue(loggedInUser);
    const errorResponse = { status: 500, statusText: 'Internal Server Error' };
    mockJobService.updateStarred.and.returnValue(throwError(errorResponse));
    // Act
    component.doToggleStarred();
    tick(); // Wait for asynchronous operation to complete
    // Assert
    expect(mockJobService.updateStarred).toHaveBeenCalledWith(1, false);
    expect(component.error).toEqual(errorResponse); // Ensure the error is set in the component
    expect(component.loading).toBeFalsy(); // Ensure loading indicator is turned off
  }));
});

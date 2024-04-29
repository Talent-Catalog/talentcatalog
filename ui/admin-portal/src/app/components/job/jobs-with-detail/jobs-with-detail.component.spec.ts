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
import {LocalStorageModule} from "angular-2-local-storage";
import {NgSelectModule} from "@ng-select/ng-select";
import {Router} from "@angular/router";
import {SortedByComponent} from "../../util/sort/sorted-by.component";
import {ChatReadStatusComponent} from "../../chat/chat-read-status/chat-read-status.component";
import {User} from "../../../model/user";

// Mocked Users
const currentUser: User = {
  approver: undefined,
  createdBy: undefined,
  createdDate: 0,
  jobCreator: false,
  lastLogin: 0,
  mfaConfigured: false,
  name: "",
  partner: undefined,
  purpose: "",
  readOnly: false,
  role: "",
  sourceCountries: [],
  status: "",
  updatedDate: 0,
  usingMfa: false,
  id: 1, username: 'testuser', firstName: 'Test', lastName: 'User', email: 'test@example.com' };
const otherUser1: User = {
  approver: undefined,
  createdBy: undefined,
  createdDate: 0,
  jobCreator: false,
  lastLogin: 0,
  mfaConfigured: false,
  name: "",
  partner: undefined,
  purpose: "",
  readOnly: false,
  role: "",
  sourceCountries: [],
  status: "",
  updatedDate: 0,
  usingMfa: false,
  id: 2, username: 'user2', firstName: 'User', lastName: 'Two', email: 'user2@example.com' };
const otherUser2: User = {
  approver: undefined,
  createdBy: undefined,
  createdDate: 0,
  jobCreator: false,
  lastLogin: 0,
  mfaConfigured: false,
  name: "",
  partner: undefined,
  purpose: "",
  readOnly: false,
  role: "",
  sourceCountries: [],
  status: "",
  updatedDate: 0,
  usingMfa: false,
  id: 3, username: 'user3', firstName: 'User', lastName: 'Three', email: 'user3@example.com' };
const otherUser3: User = {
  approver: undefined,
  createdBy: undefined,
  createdDate: 0,
  jobCreator: false,
  lastLogin: 0,
  mfaConfigured: false,
  name: "",
  partner: undefined,
  purpose: "",
  readOnly: false,
  role: "",
  sourceCountries: [],
  status: "",
  updatedDate: 0,
  usingMfa: false,
  id: 4, username: 'user3', firstName: 'User', lastName: 'Three', email: 'user3@example.com' };

// Set up job objects with different scenarios
const jobWithCurrentUser: Job = {
  closed: false,
  contactUser: undefined,
  country: undefined,
  employerEntity: undefined,
  exclusionList: undefined,
  hiringCommitment: "",
  jobCreator: undefined,
  jobOppIntake: undefined,
  jobSummary: "",
  opportunityScore: "",
  publishedBy: undefined,
  publishedDate: undefined,
  stage: undefined,
  submissionDueDate: undefined,
  submissionList: undefined,
  suggestedList: undefined,
  suggestedSearches: [],
  won: false,
  id: 1, name: 'Test Job', starringUsers: [currentUser, otherUser1] };
const jobWithoutCurrentUser: Job = {
  closed: false,
  contactUser: undefined,
  country: undefined,
  employerEntity: undefined,
  exclusionList: undefined,
  hiringCommitment: "",
  jobCreator: undefined,
  jobOppIntake: undefined,
  jobSummary: "",
  opportunityScore: "",
  publishedBy: undefined,
  publishedDate: undefined,
  stage: undefined,
  submissionDueDate: undefined,
  submissionList: undefined,
  suggestedList: undefined,
  suggestedSearches: [],
  won: false,
  id: 2, name: 'Test Job 2', starringUsers: [otherUser2, otherUser3] };
fdescribe('JobsWithDetailComponent', () => {
  let component: JobsWithDetailComponent;
  let fixture: ComponentFixture<JobsWithDetailComponent>;
  let jobService: jasmine.SpyObj<JobService>;
  let authService: jasmine.SpyObj<AuthenticationService>;

  beforeEach(waitForAsync(() => {
    const jobServiceSpy = jasmine.createSpyObj('JobService', ['checkUnreadChats','updateStarred','searchPaged']);
    const authServiceSpy = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser'], { currentUser: { id: 1 } });
    TestBed.configureTestingModule({
      declarations: [SortedByComponent,ChatReadStatusComponent,JobsWithDetailComponent,JobsComponent],
      imports: [
        HttpClientTestingModule,
        ReactiveFormsModule,
        NgbPaginationModule,
        LocalStorageModule.forRoot({}),
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
    component.onJobSelected(jobWithCurrentUser);
    expect(component.selectedJob).toEqual(jobWithCurrentUser);
  });
  it('should refresh jobs component when job is updated', () => {
    const jobsComponentSpy = jasmine.createSpyObj('JobsComponent', ['search']);
    component.jobsComponent = jobsComponentSpy;

    component.onJobUpdated({} as Job);
    expect(jobsComponentSpy.search).toHaveBeenCalled();
  });
  it('should correctly identify whether the authenticated user has starred the job', () => {
    // Mock the authentication service to return a mock user ID
    authService.getLoggedInUser.and.returnValue(currentUser);

    // Test when the authenticated user has starred the job
    component.selectedJob = jobWithCurrentUser;
    let starred = component.isStarred();
    expect(starred).toBeTruthy();

    // Test when the authenticated user has not starred the job
    component.selectedJob = jobWithoutCurrentUser;
    starred = component.isStarred();
    expect(starred).toBeFalsy();
  });
  it('should handle error when toggling starred status', fakeAsync(() => {
    const error = 'Error toggling starred status';
    jobService.updateStarred.and.returnValue(throwError(error));

    component.selectedJob = jobWithoutCurrentUser;
    component.doToggleStarred();
    tick(); // Wait for observable to resolve
    expect(component.loading).toBeFalsy();
    expect(component.error).toEqual(error);
  }));
});

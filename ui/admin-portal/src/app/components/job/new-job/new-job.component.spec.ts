import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from '@angular/core/testing';
import { ReactiveFormsModule, FormBuilder, FormsModule } from '@angular/forms';
import { NewJobComponent } from './new-job.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NgbPaginationModule } from "@ng-bootstrap/ng-bootstrap";
import { LocalStorageModule } from "angular-2-local-storage";
import { NgSelectModule } from "@ng-select/ng-select";
import { JobService } from '../../../services/job.service';
 import { SavedListService } from '../../../services/saved-list.service';
import { SlackService } from '../../../services/slack.service';
 import { of} from 'rxjs';
import {UpdateJobRequest, Job} from "../../../model/job";
 import {User} from "../../../model/user";
import {Progress} from "../../../model/base";
import {SalesforceService} from "../../../services/salesforce.service";
import {Router} from "@angular/router";
import { Directive, Input} from "@angular/core";
import {SavedList} from "../../../model/saved-list";
import {Location} from "@angular/common";
 import {Employer} from "../../../model/partner";

const mockSavedList: SavedList = { id: 123, fixed: false, global: false, name: "Test SavedList" };
const currentUser:User = {
  approver: undefined,
  createdBy: undefined,
  createdDate: 0,
  jobCreator: false,
  lastLogin: 1,
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
  id: 1, username: 'testuser', firstName: 'Test', lastName: 'User', email: 'test@example.com'
};
// Define a mock Employer object
const mockEmployer: Employer = {
  description: "Test", hasHiredInternationally: false, id: 0, sfId: "", website: "",
  // Define properties as needed for your tests
  name: 'Test Employer'
  // Add other properties if required
};
 @Directive({
  selector: '[routerLink]'
})
export class RouterLinkDirectiveStub {
  @Input('routerLink') linkParams: any;
 }
fdescribe('NewJobComponent', () => {
  let component: NewJobComponent;
  let fixture: ComponentFixture<NewJobComponent>;
  let jobService: jasmine.SpyObj<JobService>;

  let mockJobService: any;
  let mockSavedListService: any;
  let mockSalesforceService: any;
  let mockSlackService: any;
  let location: Location;
  let fb: FormBuilder;

  let fbSpy : any;
  let router: Router; // Add Router dependency here

  beforeEach(waitForAsync(() => {
    mockJobService = jasmine.createSpyObj('JobService', ['create']);
    mockSavedListService = jasmine.createSpyObj('SavedListService', ['createFolder']);
    mockSalesforceService = jasmine.createSpyObj('SalesforceService', ['updateEmployerOpportunity','joblink']);
    mockSlackService = jasmine.createSpyObj('SlackService', ['postJob']);
    location = jasmine.createSpyObj('Location', ['prepareExternalUrl']);
    jasmine.createSpyObj('FormBuilder', ['group']);
    component = new NewJobComponent(
      null, // Provide mock AuthorizationService
      null, // Provide mock AuthenticationService
      fbSpy,
      mockJobService,
      mockSalesforceService,
      mockSavedListService,
      mockSlackService,
      location, // Provide mock Location
      router
     );
    TestBed.configureTestingModule({
      declarations: [NewJobComponent,RouterLinkDirectiveStub],
      imports: [
        ReactiveFormsModule,
        HttpClientTestingModule,
        FormsModule,
        NgbPaginationModule,
        LocalStorageModule.forRoot({}),
        NgSelectModule
      ],
      providers: [
        { provide: JobService, useValue: mockJobService }, // Use jobServiceSpy instead of jobService
        { provide: SavedListService, useValue: mockSavedListService }, // Use jobServiceSpy instead of jobService
        { provide: SalesforceService, useValue: mockSalesforceService }, // Use jobServiceSpy instead of jobService
        { provide: SlackService, useValue: mockSlackService }, // Use jobServiceSpy instead of jobService
        { provide: FormBuilder },
        {
          provide: Router,
          useClass: class {
            navigateByUrl = jasmine.createSpy('navigateByUrl');
            createUrlTree = jasmine.createSpy('createUrlTree');
            serializeUrl = jasmine.createSpy('serializeUrl');
          }
        },
        { provide: Location, useValue: location }
      ]
    }).compileComponents();

    jobService = TestBed.inject(JobService) as jasmine.SpyObj<JobService>;
    router = TestBed.inject(Router);
    mockJobService = TestBed.inject(JobService) as jasmine.SpyObj<JobService>;
    mockSavedListService = TestBed.inject(SavedListService) as jasmine.SpyObj<SavedListService>;
    mockSalesforceService = TestBed.inject(SalesforceService) as jasmine.SpyObj<SalesforceService>;
    mockSlackService = TestBed.inject(SlackService) as jasmine.SpyObj<SlackService>;
   fb = TestBed.inject(FormBuilder);
   }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewJobComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
  it('should create a new job successfully', () => {
    // Mock necessary dependencies and services
    const mockJob:Job = {
      id:12,
      closed: false,
      contactUser: currentUser,
      country: undefined,
      employerEntity: undefined,
      exclusionList: undefined,
      hiringCommitment: "",
      jobCreator: undefined,
      jobOppIntake: undefined,
      jobSummary: "Test ",
      name: "Test",
      opportunityScore: "",
      publishedBy: undefined,
      publishedDate: undefined,
      stage: undefined,
      starringUsers: [],
      submissionDueDate: undefined,
      submissionList: mockSavedList,
      suggestedList: undefined,
      suggestedSearches: [],
      won: false/* Mock Job data */};
    // const mockSavedList:SavedList = {id:12,fixed: false, global: false, name: ""/* Mock SavedList data */};
    mockJobService.create.and.returnValue(of(mockJob));
    mockSavedListService.createFolder.and.returnValue(of(mockSavedList));
    mockSalesforceService.updateEmployerOpportunity.and.returnValue(of(null));
    mockSlackService.postJob.and.returnValue(of(null));


    component.doRegistration();

    // Invoke the method under test

    // // Assert that the job creation process completes successfully
    expect(component.creatingJob).toBe(Progress.Finished);
    expect(component.errorCreatingJob).toBeNull();
    expect(component.errorCreatingFolders).toBeNull();
    expect(component.errorCreatingSFLinks).toBeNull();
    expect(component.errorPostingToSlack).toBeNull();
    expect(component.job).toEqual(mockJob);
    expect(component.savedList).toEqual(mockSavedList);
  });
  it('should start job creation process when a valid job link is provided', () => {
    // Arrange
    const sfJoblink = 'valid_job_link';
    const jobName = 'Test Job';
    const request: UpdateJobRequest = { roleName: null, sfJoblink };
    jobService.create.and.returnValue(of());
    // // Act
    component.onJoblinkValidation({ valid: true, sfJoblink, jobname: jobName });
    component.doRegistration();
    // Assert
    expect(jobService.create).toHaveBeenCalledWith(request);
  });
  it('should update job name when job form changes', fakeAsync(() => {

    const roleName = 'Test Role';
    component.employer = mockEmployer;
    component.jobForm = fb.group({
      role: [roleName] // Initialize with an empty string or any default value
     });

    const spyOnMethod = spyOn<any>(component, 'subscribeToJobFormChanges').and.callThrough();
    component['subscribeToJobFormChanges']();
    component.jobForm.patchValue({ role: roleName });
    tick(1000); // Adjust the delay as needed (1 second in this example)
    expect(spyOnMethod).toHaveBeenCalled();
    expect(component.roleName).toEqual(roleName);
    expect(component.jobName).toEqual(`${component.employer.name}-${(new Date()).getFullYear()}-${roleName}`);
  }));


});

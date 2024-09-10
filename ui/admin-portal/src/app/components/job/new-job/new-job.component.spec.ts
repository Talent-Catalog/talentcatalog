import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from '@angular/core/testing';
import {FormBuilder, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NewJobComponent} from './new-job.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {NgbModal, NgbPaginationModule} from "@ng-bootstrap/ng-bootstrap";
import {LocalStorageModule} from "angular-2-local-storage";
import {NgSelectModule} from "@ng-select/ng-select";
import {JobService} from '../../../services/job.service';
import {SavedListService} from '../../../services/saved-list.service';
import {SlackService} from '../../../services/slack.service';
import {of} from 'rxjs';
import {UpdateJobRequest} from "../../../model/job";
import {Progress} from "../../../model/base";
import {SalesforceService} from "../../../services/salesforce.service";
import {Router} from "@angular/router";
import {Location} from "@angular/common";
import {MockJob} from "../../../MockData/MockJob";
import {MockEmployer} from "../../../MockData/MockEmployer";
import {MockSavedList} from "../../../MockData/MockSavedList";
import {RouterLinkStubDirective} from "../../login/login.component.spec";


describe('NewJobComponent', () => {
  let component: NewJobComponent;
  let fixture: ComponentFixture<NewJobComponent>;

  let mockJobService: any;
  let mockSavedListService: any;
  let mockSalesforceService: any;
  let mockSlackService: any;
  let location: Location;
  let fb: FormBuilder;
  let fbSpy : any;
  let router: Router; // Add Router dependency here
  let ngbModal: NgbModal; // Add Router dependency here

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
      router,
      ngbModal
     );
    TestBed.configureTestingModule({
      declarations: [NewJobComponent,RouterLinkStubDirective],
      imports: [
        ReactiveFormsModule,
        HttpClientTestingModule,
        FormsModule,
        NgbPaginationModule,
        LocalStorageModule.forRoot({}),
        NgSelectModule
      ],
      providers: [
        { provide: JobService, useValue: mockJobService },
        { provide: SavedListService, useValue: mockSavedListService },
        { provide: SalesforceService, useValue: mockSalesforceService },
        { provide: SlackService, useValue: mockSlackService },
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
    mockJobService.create.and.returnValue(of(MockJob));
    mockSavedListService.createFolder.and.returnValue(of(MockSavedList));
    mockSalesforceService.updateEmployerOpportunity.and.returnValue(of(true));
    component.doRegistration();
    // // Assert that the job creation process completes successfully
    expect(component.creatingJob).toBe(Progress.Finished);
    expect(component.errorCreatingJob).toBeNull();
    expect(component.errorCreatingFolders).toBeNull();
    expect(component.errorCreatingSFLinks).toBeNull();
    expect(component.errorPostingToSlack).toBeNull();
    expect(component.job).toEqual(MockJob);
    expect(component.savedList).toEqual(MockSavedList);
  });
  it('should start job creation process when a valid job link is provided', () => {
    // Arrange
    const sfJoblink = 'valid_job_link';
    const request: UpdateJobRequest = { roleName: null, sfJoblink,jobToCopyId: null };
    mockJobService.create.and.returnValue(of());
    // // Act
    component.onSfJoblinkValidation({ valid: true, sfJoblink, jobname: MockJob.name });
    component.doRegistration();
    // Assert
    expect(mockJobService.create).toHaveBeenCalledWith(request);
  });
  it('should update job name when job form changes', fakeAsync(() => {

    const roleName = 'Test Role';
    component.employer = new MockEmployer();
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

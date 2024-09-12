import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';

import {CandidateVisaJobComponent} from './candidate-visa-job.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
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

describe('CandidateVisaJobComponent', () => {
  let component: CandidateVisaJobComponent;
  let fixture: ComponentFixture<CandidateVisaJobComponent>;
  let candidateVisaJobServiceMock: jasmine.SpyObj<CandidateVisaJobService>;
  let modalServiceMock: jasmine.SpyObj<NgbModal>;
  let authServiceMock: jasmine.SpyObj<AuthorizationService>;
  const mockCandidate = new MockCandidate();

  beforeEach(async () => {
    candidateVisaJobServiceMock = jasmine.createSpyObj('CandidateService', ['create', 'delete', 'get']);
    modalServiceMock = jasmine.createSpyObj('NgbModal', ['open']);
    authServiceMock = jasmine.createSpyObj('AuthorizationService', ['isSystemAdminOnly']);
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule],
      declarations: [CandidateVisaJobComponent],
      providers: [
        FormBuilder,
        { provide: CandidateVisaJobService, useValue: candidateVisaJobServiceMock },
        { provide: NgbModal, useValue: modalServiceMock },
        { provide: AuthorizationService, useValue: authServiceMock },
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateVisaJobComponent);
    component = fixture.componentInstance;
    component.candidate = new MockCandidate();
    component.candidateIntakeData = mockCandidateIntakeData;
    component.visaCheckRecord = MockCandidateVisa;
    component.selectedJob = MockCandidateVisaJobCheck;


    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit the first job by default on init', () => {
    spyOn(component.selectedJobChange, 'emit');
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
    modalServiceMock.open.and.returnValue(modalRef as any);

    // Mock the create service call
    candidateVisaJobServiceMock.create.and.returnValue(of(newVisaJobCheck));

    spyOn(component.selectedJobChange, 'emit');

    // Simulate button click to add a country
    component.addJob();
    tick();

    fixture.detectChanges();

    // Verify the service call and the update to visaChecks
    expect(candidateVisaJobServiceMock.create).toHaveBeenCalledWith(1, { jobOppId: job.id });
    expect(component.form.value.jobIndex).toBe(1);
    expect(component.selectedJobChange.emit).toHaveBeenCalledWith(newVisaJobCheck);
  }));

});

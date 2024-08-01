import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateVisaJobComponent} from './candidate-visa-job.component';
import {CandidateVisaJobService} from "../../../../../../services/candidate-visa-job.service";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {MockCandidateVisaJobCheck} from "../../../../../../MockData/MockCandidateVisaCheck";
import {MockCandidate} from "../../../../../../MockData/MockCandidate";
import {MockCandidateVisa} from "../../../../../../MockData/MockCandidateVisa";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {RouterLinkStubDirective} from "../../../../../login/login.component.spec";
import {of} from "rxjs";
import {ShortJob} from "../../../../../../model/job";
import {AuthorizationService} from "../../../../../../services/authorization.service";
// todo these tests - maybe if these are completed it might fix the bug?
fdescribe('CandidateVisaJobComponent', () => {
  let component: CandidateVisaJobComponent;
  let fixture: ComponentFixture<CandidateVisaJobComponent>;
  let candidateVisaJobServiceSpy: jasmine.SpyObj<CandidateVisaJobService>;
  let authServiceSpy: jasmine.SpyObj<AuthorizationService>;
  const mockCandidate = new MockCandidate();
  let mockModalService: any;

  beforeEach(async () => {
    const visaJobSpyObj = jasmine.createSpyObj('CandidateVisaJobService', ['create', 'get', 'delete']);
    const authorizeServiceSpyObj = jasmine.createSpyObj('AuthorizationService', ['isSystemAdminOnly']);
    mockModalService = jasmine.createSpyObj('NgbModal', ['open']);
    await TestBed.configureTestingModule({
      declarations: [ CandidateVisaJobComponent,RouterLinkStubDirective ],
      imports: [NgSelectModule,FormsModule,ReactiveFormsModule,HttpClientTestingModule],
      providers: [
        FormBuilder,
        { provide: AuthorizationService, useValue: authorizeServiceSpyObj },
        { provide: CandidateVisaJobService, useValue: visaJobSpyObj },
        { provide: NgbModal, useValue: mockModalService }
      ],
    })
    .compileComponents();
    candidateVisaJobServiceSpy = TestBed.inject(CandidateVisaJobService) as jasmine.SpyObj<CandidateVisaJobService>;
    authServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateVisaJobComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    component.visaCheckRecord = MockCandidateVisa;
    component.selectedJob = MockCandidateVisaJobCheck;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit the first job in the array on init', () => {
    spyOn(component.selectedJobChange, 'emit');
    component.ngOnInit();
    // Check if the selected job change event is emitted with the correct argument
    expect(component.selectedJobChange.emit).toHaveBeenCalledWith(MockCandidateVisaJobCheck);
  });

  // it('should call addJob and open HasNameSelectorComponent modal', () => {
  //   mockModalService.open.and.returnValue({
  //     componentInstance: {},
  //     result: Promise.resolve('some result'),
  //     close: () => {}, // Mock close method
  //   } as NgbModalRef);
  //   component.addJob();
  //   expect(mockModalService.open).toHaveBeenCalledWith(HasNameSelectorComponent);
  // });

  it('should create new visa job check when create service method called', () => {
    const mockShortJob: ShortJob = {id: 1, name: "TestJob"};
    candidateVisaJobServiceSpy.create.and.returnValue(of(MockCandidateVisaJobCheck));
    spyOn(component.selectedJobChange, 'emit');

    component.createVisaJobCheck(mockShortJob);

    expect(candidateVisaJobServiceSpy.create.calls.count()).toBe(1);
    expect(component.selectedJobChange.emit).toHaveBeenCalledWith(MockCandidateVisaJobCheck);
  });

  // it('should call delete and open ConfirmationComponent modal', () => {
  //   mockModalService.open.and.returnValue({
  //     componentInstance: {
  //       message: ""
  //     },
  //     result: Promise.resolve(true),
  //     close: () => {}, // Mock close method
  //   } as NgbModalRef);
  //   spyOn(component.selectedJobChange, 'emit');
  //   fixture.detectChanges();
  //   component.deleteJob(0);
  //
  //   expect(mockModalService.open).toHaveBeenCalledWith(ConfirmationComponent);
  // });
  //
  // it('should call delete and delete object', fakeAsync(() => {
  //   candidateVisaJobServiceSpy.delete.and.returnValue(of(true));
  //   candidateVisaJobServiceSpy.get.and.returnValue(of(MockCandidateVisaJobCheck));
  //   spyOn(component.selectedJobChange, 'emit');
  //
  //   component['doDelete'](0)
  //   tick();
  //
  //   expect(candidateVisaJobServiceSpy.delete.calls.count()).toBe(1);
  //   expect(component.form.get('jobIndex').value).toEqual(0);
  //   expect(candidateVisaJobServiceSpy.get.calls.count()).toBe(1);
  // }));

  it('should return true if user is a system admin only', () => {
    authServiceSpy.isSystemAdminOnly.and.returnValue(true);
    expect(component.canDeleteVisaJob()).toBeTrue();
  });

  it('should return false if user is not a system admin only', () => {
    authServiceSpy.isSystemAdminOnly.and.returnValue(false);
    expect(component.canDeleteVisaJob()).toBeFalse();
  });
});

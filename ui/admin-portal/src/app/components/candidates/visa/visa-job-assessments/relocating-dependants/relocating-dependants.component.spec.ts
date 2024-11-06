import {ComponentFixture, TestBed} from '@angular/core/testing';
import {UntypedFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {RelocatingDependantsComponent} from './relocating-dependants.component';
import {CandidateVisaCheckService} from '../../../../../services/candidate-visa-check.service';
import {By} from '@angular/platform-browser';
import {NgSelectModule} from '@ng-select/ng-select';
import {CUSTOM_ELEMENTS_SCHEMA, DebugElement} from '@angular/core';
import {CandidateDependant, DependantRelations} from "../../../../../model/candidate";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {of, throwError} from "rxjs";
import {mockCandidateOpportunity} from "../../../../../MockData/MockCandidateOpportunity";
import {CandidateOpportunityService} from "../../../../../services/candidate-opportunity.service";
import {CandidateDependantService} from "../../../../../services/candidate-dependant.service";
import {CandidateOpportunity} from "../../../../../model/candidate-opportunity";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {LocalStorageModule} from "angular-2-local-storage";
import {AuthorizationService} from "../../../../../services/authorization.service";

describe('RelocatingDependantsComponent', () => {
  let component: RelocatingDependantsComponent;
  let fixture: ComponentFixture<RelocatingDependantsComponent>;
  let candidateVisaCheckService: jasmine.SpyObj<CandidateVisaCheckService>;
  let candidateOpportunityService: jasmine.SpyObj<CandidateOpportunityService>;
  let candidateDependantService: jasmine.SpyObj<CandidateDependantService>;
  let authorizationService: jasmine.SpyObj<AuthorizationService>;
  let fb: UntypedFormBuilder;

  const mockCandidate = new MockCandidate();
  const mockOpp: CandidateOpportunity = mockCandidateOpportunity;
  const mockDependants: CandidateDependant[] = [
    { id: 1, relation: DependantRelations.Partner, name: 'John Doe' },
    { id: 2, relation: DependantRelations.Child, name: 'Jane Doe' }
  ];

  beforeEach(async () => {
    const candidateVisaCheckServiceSpy = jasmine.createSpyObj('CandidateVisaCheckService', ['someMethod']);
    const candidateOpportunityServiceSpy = jasmine.createSpyObj('CandidateOpportunityService', ['updateSfCaseRelocationInfo']);
    const candidateDependantServiceSpy = jasmine.createSpyObj('CandidateDependantService', ['list']);
    const authorizationServiceSpy = jasmine.createSpyObj('AuthorizationService',
      ['isReadOnly']);

    await TestBed.configureTestingModule({
      declarations: [RelocatingDependantsComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule, NgSelectModule,
        LocalStorageModule.forRoot({}),
      ],
      providers: [
        { provide: UntypedFormBuilder  },
        { provide: CandidateVisaCheckService, useValue: candidateVisaCheckServiceSpy },
        { provide: CandidateOpportunityService, useValue: candidateOpportunityServiceSpy },
        { provide: CandidateDependantService, useValue: candidateDependantServiceSpy },
        { provide: AuthorizationService, useValue: authorizationServiceSpy }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    candidateVisaCheckService = TestBed.inject(CandidateVisaCheckService) as jasmine.SpyObj<CandidateVisaCheckService>;
    candidateOpportunityService = TestBed.inject(CandidateOpportunityService) as jasmine.SpyObj<CandidateOpportunityService>;
    candidateDependantService = TestBed.inject(CandidateDependantService) as jasmine.SpyObj<CandidateDependantService>;
    authorizationService = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    fb = TestBed.inject(UntypedFormBuilder);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RelocatingDependantsComponent);
    component = fixture.componentInstance;
    component.candidateId = mockCandidate.id;
    component.candidateOpp = mockOpp;
    candidateDependantService.list.and.returnValue(of(mockDependants));
    authorizationService.isReadOnly.and.returnValue(false);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch dependants on init', () => {
    component.ngOnInit();
    component.fetchDependants();
    expect(candidateDependantService.list).toHaveBeenCalledWith(component.candidateId);
    expect(component.dependants).toEqual(mockDependants);
    expect(component.loading).toBeFalse();
  });

  it('should initialize form with relocatingDependantIds control', () => {
    expect(component.form.contains('relocatingDependantIds')).toBeTrue();
  });

  it('should render autosave status component', () => {
    const autosaveStatusComponent = fixture.debugElement.query(By.css('app-autosave-status'));
    expect(autosaveStatusComponent).toBeTruthy();
  });

  it('should display error message when error is set', () => {
    component.error = 'Some error message';
    fixture.detectChanges();
    const errorElement: HTMLElement = fixture.nativeElement.querySelector('div');
    expect(errorElement.textContent).toContain('Some error message');
  });

  it('should render ng-select with correct placeholder', () => {
    const ngSelect: DebugElement = fixture.debugElement.query(By.css('ng-select'));
    expect(ngSelect).toBeTruthy();
    expect(ngSelect.attributes['placeholder']).toBe('Select or type...');
  });

  it('should display the correct helper text', () => {
    const helperText: HTMLElement = fixture.nativeElement.querySelector('.form-text');
    expect(helperText.textContent).toContain("If a dependant isn't listed in the dropdown, you may need to add the dependant to the Dependants section under the Full Intake tab.");
  });

  it('should handle error when updating case stats', () => {
    candidateOpportunityService.updateSfCaseRelocationInfo.and.returnValue(throwError('Error updating case stats'));

    component.requestSfCaseRelocationInfoUpdate();

    expect(candidateOpportunityService.updateSfCaseRelocationInfo).toHaveBeenCalledWith(component.candidateOpp.id);
    expect(component.error).toBe('Error updating case stats');
    expect(component.loading).toBeFalse();
  });
});

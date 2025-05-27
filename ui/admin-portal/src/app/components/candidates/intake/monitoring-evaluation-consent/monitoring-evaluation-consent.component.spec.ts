import {MonitoringEvaluationConsentComponent} from './monitoring-evaluation-consent.component';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {NgSelectModule} from '@ng-select/ng-select';
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {YesNo} from '../../../../model/candidate';
import {AutosaveStatusComponent} from '../../../util/autosave-status/autosave-status.component';
import {of, throwError} from 'rxjs';
import {AuthorizationService} from '../../../../services/authorization.service';

describe('MonitoringEvaluationConsentComponent', () => {
  let component: MonitoringEvaluationConsentComponent;
  let fixture: ComponentFixture<MonitoringEvaluationConsentComponent>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let authServiceSpy: jasmine.SpyObj<AuthorizationService>;

  beforeEach(async () => {
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['createUpdateLiveCandidate', 'updateCandidate']);
    authServiceSpy = jasmine.createSpyObj('AuthorizationService', ['canAccessSalesforce']);

    await TestBed.configureTestingModule({
      declarations: [MonitoringEvaluationConsentComponent, AutosaveStatusComponent],
      imports: [HttpClientTestingModule, NgSelectModule, FormsModule, ReactiveFormsModule],
      providers: [
        UntypedFormBuilder,
        {provide: CandidateService, useValue: candidateServiceSpy},
        {provide: AuthorizationService, useValue: authServiceSpy}
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MonitoringEvaluationConsentComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {monitoringEvaluationConsent: YesNo.Yes};
    component.editable = true;
    component.entity = {id:1}
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form control with the correct default value', () => {
    expect(component.form.get('monitoringEvaluationConsent').value).toBe(YesNo.Yes);
  });

  it('should call createUpdateLiveCandidate on success', () => {
    candidateServiceSpy.createUpdateLiveCandidate.and.returnValue(of());
    component.createUpdateSalesforce();
    expect(candidateServiceSpy.createUpdateLiveCandidate).toHaveBeenCalledWith(1);
    expect(component.error).toBeNull();
  });

  it('should handle error in createUpdateSalesforce', () => {
    const mockError = { message: 'Something went wrong' } as any;
    candidateServiceSpy.createUpdateLiveCandidate.and.returnValue(throwError(() => mockError));
    component.createUpdateSalesforce();
    expect(component.loading).toBeFalse();
    candidateServiceSpy.createUpdateLiveCandidate.and.returnValue(throwError(() => mockError));
  });

  it('should return correct value from canAccessSalesforce()', () => {
    authServiceSpy.canAccessSalesforce.and.returnValue(true);
    expect(component.canAccessSalesforce()).toBeTrue();
    authServiceSpy.canAccessSalesforce.and.returnValue(false);
    expect(component.canAccessSalesforce()).toBeFalse();
  });
});

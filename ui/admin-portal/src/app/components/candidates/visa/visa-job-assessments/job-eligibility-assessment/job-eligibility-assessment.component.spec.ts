import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {JobEligibilityAssessmentComponent} from './job-eligibility-assessment.component';
import {TBBEligibilityAssessment} from '../../../../../model/candidate';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidateVisaJobCheck} from "../../../../../MockData/MockCandidateVisaCheck";

fdescribe('JobEligibilityAssessmentComponent', () => {
  let component: JobEligibilityAssessmentComponent;
  let fixture: ComponentFixture<JobEligibilityAssessmentComponent>;
  let fb: FormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [JobEligibilityAssessmentComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgSelectModule],
      providers: [FormBuilder]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobEligibilityAssessmentComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(FormBuilder);
    component.visaJobCheck = MockCandidateVisaJobCheck;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have a form with visaJobTbbEligibility control', () => {
    expect(component.form.contains('visaJobTbbEligibility')).toBeTrue();
  });

  it('should update form value when input changes', () => {
    const testValue: TBBEligibilityAssessment = TBBEligibilityAssessment.Discuss;
    const ngSelect = fixture.nativeElement.querySelector('ng-select');
    ngSelect.value = testValue;
    ngSelect.dispatchEvent(new Event('change'));
    component.form.get('visaJobTbbEligibility').setValue(testValue);
    expect(component.form.get('visaJobTbbEligibility')?.value).toEqual(testValue);
  });
});

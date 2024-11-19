import {ComponentFixture, TestBed} from '@angular/core/testing';
import {UntypedFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {JobFamilyAusComponent} from './job-family-aus.component';
import {CandidateVisaJobCheck, YesNo} from '../../../../../model/candidate';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidateVisaJobCheck} from "../../../../../MockData/MockCandidateVisaCheck";

describe('JobFamilyAusComponent', () => {
  let component: JobFamilyAusComponent;
  let fixture: ComponentFixture<JobFamilyAusComponent>;
  let fb: UntypedFormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [JobFamilyAusComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgSelectModule],
      providers: [UntypedFormBuilder]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobFamilyAusComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(UntypedFormBuilder);
    component.visaCheckRecord = MockCandidateVisaJobCheck[0];
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have a form with visaJobFamilyAus control', () => {
    expect(component.form.contains('visaJobFamilyAus')).toBeTrue();
  });

  it('should update form value when input changes', () => {
    const testValue: YesNo = YesNo.Yes;
    component.selectedJobCheck = { id: 1, familyAus: testValue } as CandidateVisaJobCheck;
    component.ngOnInit();
    expect(component.form.get('visaJobFamilyAus')?.value).toEqual(testValue);
  });
});

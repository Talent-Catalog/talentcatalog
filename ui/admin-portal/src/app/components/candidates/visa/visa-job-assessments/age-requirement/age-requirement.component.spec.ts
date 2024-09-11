import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {NgSelectModule} from '@ng-select/ng-select';
import {AgeRequirementComponent} from './age-requirement.component';
import {MockCandidateVisaJobCheck} from "../../../../../MockData/MockCandidateVisaCheck";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";

describe('AgeRequirementComponent', () => {
  let component: AgeRequirementComponent;
  let fixture: ComponentFixture<AgeRequirementComponent>;
  let fb: FormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AgeRequirementComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule, NgSelectModule],
      providers: [FormBuilder]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AgeRequirementComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(FormBuilder);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with correct values', () => {
    const expectedAgeRequirement = 'Yes'; // Assuming 'Yes' is a valid initial value
    component.visaJobCheck = MockCandidateVisaJobCheck;
    component.ngOnInit();
    expect(component.form.get('visaJobAgeRequirement').value).toBe(expectedAgeRequirement);
  });
});

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {UntypedFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {NgSelectModule} from '@ng-select/ng-select';
import {AgeRequirementComponent} from './age-requirement.component';
import {MockCandidateVisaJobCheck} from "../../../../../MockData/MockCandidateVisaCheck";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";

describe('AgeRequirementComponent', () => {
  let component: AgeRequirementComponent;
  let fixture: ComponentFixture<AgeRequirementComponent>;
  let fb: UntypedFormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AgeRequirementComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule, NgSelectModule],
      providers: [UntypedFormBuilder]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AgeRequirementComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(UntypedFormBuilder);
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

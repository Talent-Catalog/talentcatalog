import {ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaEligibilityAssessmentComponent} from './visa-eligibility-assessment.component';

describe('VisaEligibilityComponent', () => {
  let component: VisaEligibilityAssessmentComponent;
  let fixture: ComponentFixture<VisaEligibilityAssessmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VisaEligibilityAssessmentComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaEligibilityAssessmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

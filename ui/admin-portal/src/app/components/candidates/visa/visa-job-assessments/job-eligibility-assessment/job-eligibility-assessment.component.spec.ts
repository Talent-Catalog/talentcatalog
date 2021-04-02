import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {JobEligibilityAssessmentComponent} from './job-eligibility-assessment.component';

describe('JobEligibilityAssessmentComponent', () => {
  let component: JobEligibilityAssessmentComponent;
  let fixture: ComponentFixture<JobEligibilityAssessmentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JobEligibilityAssessmentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JobEligibilityAssessmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

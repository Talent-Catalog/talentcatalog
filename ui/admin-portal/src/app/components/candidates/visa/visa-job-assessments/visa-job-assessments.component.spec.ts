import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaJobAssessmentsComponent} from './visa-job-assessments.component';

describe('VisaJobAssessmentComponent', () => {
  let component: VisaJobAssessmentsComponent;
  let fixture: ComponentFixture<VisaJobAssessmentsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaJobAssessmentsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaJobAssessmentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

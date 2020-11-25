import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaJobAssessmentComponent} from './visa-job-assessment.component';

describe('VisaJobAssessmentComponent', () => {
  let component: VisaJobAssessmentComponent;
  let fixture: ComponentFixture<VisaJobAssessmentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaJobAssessmentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaJobAssessmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaJobAssessmentCardComponent} from './visa-job-assessment-card.component';

describe('VisaJobAssessmentCardComponent', () => {
  let component: VisaJobAssessmentCardComponent;
  let fixture: ComponentFixture<VisaJobAssessmentCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaJobAssessmentCardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaJobAssessmentCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

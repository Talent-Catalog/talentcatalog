import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaJobAssessmentAuComponent} from './visa-job-assessment-au.component';

describe('VisaJobAssessmentComponent', () => {
  let component: VisaJobAssessmentAuComponent;
  let fixture: ComponentFixture<VisaJobAssessmentAuComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaJobAssessmentAuComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaJobAssessmentAuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

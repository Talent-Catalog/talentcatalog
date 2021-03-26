import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaJobAssessmentCaComponent} from './visa-job-assessment-ca.component';

describe('VisaJobAssessmentCaComponent', () => {
  let component: VisaJobAssessmentCaComponent;
  let fixture: ComponentFixture<VisaJobAssessmentCaComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaJobAssessmentCaComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaJobAssessmentCaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

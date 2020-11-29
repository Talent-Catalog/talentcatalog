import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaFinalAssessmentComponent} from './visa-final-assessment.component';

describe('VisaJobAssessmentComponent', () => {
  let component: VisaFinalAssessmentComponent;
  let fixture: ComponentFixture<VisaFinalAssessmentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaFinalAssessmentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaFinalAssessmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

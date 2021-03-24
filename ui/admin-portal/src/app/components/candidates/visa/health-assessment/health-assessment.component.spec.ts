import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {HealthAssessmentComponent} from './health-assessment.component';

describe('HealthAssessmentComponent', () => {
  let component: HealthAssessmentComponent;
  let fixture: ComponentFixture<HealthAssessmentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HealthAssessmentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HealthAssessmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

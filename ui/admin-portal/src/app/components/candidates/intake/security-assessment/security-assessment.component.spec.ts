import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SecurityAssessmentComponent} from './security-assessment.component';

describe('SecurityAssessmentComponent', () => {
  let component: SecurityAssessmentComponent;
  let fixture: ComponentFixture<SecurityAssessmentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SecurityAssessmentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurityAssessmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

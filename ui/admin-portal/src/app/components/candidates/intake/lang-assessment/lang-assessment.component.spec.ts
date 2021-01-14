import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LangAssessmentComponent} from './lang-assessment.component';

describe('LangAssessmentComponent', () => {
  let component: LangAssessmentComponent;
  let fixture: ComponentFixture<LangAssessmentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LangAssessmentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LangAssessmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewCandidateSurveyComponent} from './view-candidate-survey.component';

describe('ViewCandidateSurveyComponent', () => {
  let component: ViewCandidateSurveyComponent;
  let fixture: ComponentFixture<ViewCandidateSurveyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ViewCandidateSurveyComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateSurveyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

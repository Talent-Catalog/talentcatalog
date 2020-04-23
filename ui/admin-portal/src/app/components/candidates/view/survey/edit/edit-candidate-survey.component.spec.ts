import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {EditCandidateSurveyComponent} from './edit-candidate-survey.component';

describe('EditCandidateSurveyComponent', () => {
  let component: EditCandidateSurveyComponent;
  let fixture: ComponentFixture<EditCandidateSurveyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditCandidateSurveyComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateSurveyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

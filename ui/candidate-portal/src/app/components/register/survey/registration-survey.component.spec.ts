import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationSurveyComponent} from './registration-survey.component';

describe('RegistrationSurveyComponent', () => {
  let component: RegistrationSurveyComponent;
  let fixture: ComponentFixture<RegistrationSurveyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationSurveyComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationSurveyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

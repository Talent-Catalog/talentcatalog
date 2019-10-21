import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateJobExperienceFormComponent} from './candidate-job-experience-form.component';

describe('CandidateWorkExperienceFormComponent', () => {
  let component: CandidateJobExperienceFormComponent;
  let fixture: ComponentFixture<CandidateJobExperienceFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateJobExperienceFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateJobExperienceFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

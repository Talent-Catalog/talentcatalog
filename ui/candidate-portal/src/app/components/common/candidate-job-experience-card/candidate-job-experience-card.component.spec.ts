import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateJobExperienceCardComponent} from './candidate-job-experience-card.component';

describe('CandidateWorkExperienceCardComponent', () => {
  let component: CandidateJobExperienceCardComponent;
  let fixture: ComponentFixture<CandidateJobExperienceCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateJobExperienceCardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateJobExperienceCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

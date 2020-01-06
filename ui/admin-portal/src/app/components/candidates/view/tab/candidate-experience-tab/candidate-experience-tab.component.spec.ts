import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateExperienceTabComponent} from './candidate-experience-tab.component';

describe('CandidateExperienceTabComponent', () => {
  let component: CandidateExperienceTabComponent;
  let fixture: ComponentFixture<CandidateExperienceTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateExperienceTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateExperienceTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

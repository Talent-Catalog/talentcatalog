import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateEducationTabComponent} from './candidate-education-tab.component';

describe('CandidateEligibilityTabComponent', () => {
  let component: CandidateEducationTabComponent;
  let fixture: ComponentFixture<CandidateEducationTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateEducationTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateEducationTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

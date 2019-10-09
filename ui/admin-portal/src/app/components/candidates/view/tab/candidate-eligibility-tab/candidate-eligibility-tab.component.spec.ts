import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateEligibilityTabComponent} from './candidate-eligibility-tab.component';

describe('CandidateEligibilityTabComponent', () => {
  let component: CandidateEligibilityTabComponent;
  let fixture: ComponentFixture<CandidateEligibilityTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateEligibilityTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateEligibilityTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

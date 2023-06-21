import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateJobsTabComponent} from './candidate-jobs-tab.component';

describe('CandidateJobsTabComponent', () => {
  let component: CandidateJobsTabComponent;
  let fixture: ComponentFixture<CandidateJobsTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateJobsTabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateJobsTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

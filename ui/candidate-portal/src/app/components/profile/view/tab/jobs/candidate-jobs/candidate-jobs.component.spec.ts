import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateJobsComponent} from './candidate-jobs.component';

describe('CandidateJobsComponent', () => {
  let component: CandidateJobsComponent;
  let fixture: ComponentFixture<CandidateJobsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateJobsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateJobsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

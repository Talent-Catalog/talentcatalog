import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewCandidateJobsComponent} from './view-candidate-jobs.component';

describe('ViewCandidateJobsComponent', () => {
  let component: ViewCandidateJobsComponent;
  let fixture: ComponentFixture<ViewCandidateJobsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewCandidateJobsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateJobsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

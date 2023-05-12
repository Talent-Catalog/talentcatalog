import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateOppsComponent} from './candidate-opps.component';

describe('CandidateJobsComponent', () => {
  let component: CandidateOppsComponent;
  let fixture: ComponentFixture<CandidateOppsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateOppsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateOppsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

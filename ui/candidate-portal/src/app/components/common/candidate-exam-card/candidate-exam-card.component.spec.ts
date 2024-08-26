import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CandidateExamCardComponent } from './candidate-exam-card.component';

describe('CandidateExamCardComponent', () => {
  let component: CandidateExamCardComponent;
  let fixture: ComponentFixture<CandidateExamCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateExamCardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateExamCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

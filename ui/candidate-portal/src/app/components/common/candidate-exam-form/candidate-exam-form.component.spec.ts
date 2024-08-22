import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CandidateExamFormComponent } from './candidate-exam-form.component';

describe('CandidateExamFormComponent', () => {
  let component: CandidateExamFormComponent;
  let fixture: ComponentFixture<CandidateExamFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateExamFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateExamFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

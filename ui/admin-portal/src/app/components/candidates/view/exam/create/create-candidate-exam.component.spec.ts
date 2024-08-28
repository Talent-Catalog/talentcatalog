import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateCandidateExamComponent } from './create-candidate-exam.component';

describe('CreateCandidateExamComponent', () => {
  let component: CreateCandidateExamComponent;
  let fixture: ComponentFixture<CreateCandidateExamComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CreateCandidateExamComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateCandidateExamComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditCandidateExamComponent } from './edit-candidate-exam.component';

describe('EditCandidateExamComponent', () => {
  let component: EditCandidateExamComponent;
  let fixture: ComponentFixture<EditCandidateExamComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditCandidateExamComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateExamComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

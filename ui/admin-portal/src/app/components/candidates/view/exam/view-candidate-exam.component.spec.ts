import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewCandidateExamComponent } from './view-candidate-exam.component';

describe('ViewCandidateExamComponent', () => {
  let component: ViewCandidateExamComponent;
  let fixture: ComponentFixture<ViewCandidateExamComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewCandidateExamComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateExamComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

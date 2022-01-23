import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AssignTasksCandidateComponent} from './assign-tasks-candidate.component';

describe('AssignTasksCandidateComponent', () => {
  let component: AssignTasksCandidateComponent;
  let fixture: ComponentFixture<AssignTasksCandidateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AssignTasksCandidateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssignTasksCandidateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

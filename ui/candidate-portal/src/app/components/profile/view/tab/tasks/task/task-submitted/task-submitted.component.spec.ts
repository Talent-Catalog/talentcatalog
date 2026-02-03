import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TaskSubmittedComponent} from './task-submitted.component';

describe('TaskSubmittedModalComponent', () => {
  let component: TaskSubmittedComponent;
  let fixture: ComponentFixture<TaskSubmittedComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TaskSubmittedComponent]
    });
    fixture = TestBed.createComponent(TaskSubmittedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TaskAbandonedComponent} from './task-abandoned.component';

describe('TaskAbandonedComponent', () => {
  let component: TaskAbandonedComponent;
  let fixture: ComponentFixture<TaskAbandonedComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TaskAbandonedComponent]
    });
    fixture = TestBed.createComponent(TaskAbandonedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TasksMonitorComponent } from './tasks-monitor.component';

describe('TasksMonitorComponent', () => {
  let component: TasksMonitorComponent;
  let fixture: ComponentFixture<TasksMonitorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TasksMonitorComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TasksMonitorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

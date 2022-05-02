import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TasksMonitorListComponent} from './tasks-monitor-list.component';

describe('TasksMonitorListComponent', () => {
  let component: TasksMonitorListComponent;
  let fixture: ComponentFixture<TasksMonitorListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TasksMonitorListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TasksMonitorListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

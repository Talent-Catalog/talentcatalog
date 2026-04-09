import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {TranslateModule} from '@ngx-translate/core';

import {TaskAbandonedComponent} from './task-abandoned.component';
import {TaskAssignment} from '../../../../../../../model/task-assignment';

@Component({selector: 'tc-button', template: '<ng-content></ng-content>'})
class TcButtonStubComponent {
  @Input() size?: string;
  @Output() onClick = new EventEmitter<void>();
}

function makeTaskAssignment(overrides: Partial<TaskAssignment> = {}): TaskAssignment {
  return {
    id: 1,
    abandonedDate: new Date('2024-01-01'),
    ...overrides
  } as TaskAssignment;
}

describe('TaskAbandonedComponent', () => {
  let component: TaskAbandonedComponent;
  let fixture: ComponentFixture<TaskAbandonedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TaskAbandonedComponent, TcButtonStubComponent],
      imports: [TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(TaskAbandonedComponent);
    component = fixture.componentInstance;
    component.selectedTask = makeTaskAssignment();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the return tc-button with the migrated size', () => {
    const button = fixture.debugElement.query(By.directive(TcButtonStubComponent));

    expect(button.componentInstance.size).toBe('sm');
  });

  it('should emit onReturnToTasksClick when returnToTasksClicked is called', () => {
    const returnSpy = spyOn(component.onReturnToTasksClick, 'emit');

    component.returnToTasksClicked();

    expect(returnSpy).toHaveBeenCalled();
  });
});

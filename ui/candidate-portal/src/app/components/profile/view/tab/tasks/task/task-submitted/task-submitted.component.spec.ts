import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {TranslateModule} from '@ngx-translate/core';

import {TaskSubmittedComponent} from './task-submitted.component';
import {TaskAssignment} from '../../../../../../../model/task-assignment';

@Component({selector: 'tc-button', template: '<ng-content></ng-content>'})
class TcButtonStubComponent {
  @Input() size?: string;
  @Output() onClick = new EventEmitter<void>();
}

function makeTaskAssignment(overrides: Partial<TaskAssignment> = {}): TaskAssignment {
  return {
    id: 1,
    completedDate: new Date('2024-01-01'),
    ...overrides
  } as TaskAssignment;
}

describe('TaskSubmittedModalComponent', () => {
  let component: TaskSubmittedComponent;
  let fixture: ComponentFixture<TaskSubmittedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TaskSubmittedComponent, TcButtonStubComponent],
      imports: [TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(TaskSubmittedComponent);
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

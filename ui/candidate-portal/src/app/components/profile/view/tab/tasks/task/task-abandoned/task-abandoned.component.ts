import {Component, EventEmitter, Input, Output, ChangeDetectionStrategy} from '@angular/core';
import {TaskAssignment} from "../../../../../../../model/task-assignment";

@Component({
  selector: 'app-task-abandoned',
  templateUrl: './task-abandoned.component.html',
  styleUrls: ['./task-abandoned.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class TaskAbandonedComponent {
  @Input() selectedTask: TaskAssignment;
  @Output() onReturnToTasksClick = new EventEmitter();

  public returnToTasksClicked() {
    this.onReturnToTasksClick.emit();
  }
}

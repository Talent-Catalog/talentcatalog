import {Component, EventEmitter, Input, Output} from '@angular/core';
import {TaskAssignment} from "../../../../../../../model/task-assignment";

@Component({
  selector: 'app-task-submitted',
  templateUrl: './task-submitted.component.html',
  styleUrls: ['./task-submitted.component.scss']
})
export class TaskSubmittedComponent {
  @Input() selectedTask: TaskAssignment;
  @Output() onReturnToTasksClick = new EventEmitter();

  public returnToTasksClicked() {
    this.onReturnToTasksClick.emit();
  }
}

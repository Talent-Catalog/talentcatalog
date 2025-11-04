import {Component, EventEmitter, Output} from '@angular/core';

@Component({
  selector: 'app-task-submitted-modal',
  templateUrl: './task-submitted.component.html',
  styleUrls: ['./task-submitted.component.scss']
})
export class TaskSubmittedComponent {
  constructor() {}

  @Output() onReturnToTasksClick = new EventEmitter<void>();
  @Output() onStayOnTaskClick = new EventEmitter<void>();

  returnToTasksClick(): void {
    this.onReturnToTasksClick.emit();
  }

  stayOnTaskClick(): void {
    this.onStayOnTaskClick.emit();
  }
}

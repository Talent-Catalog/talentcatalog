import {Component, EventEmitter, Output} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-task-submitted-modal',
  templateUrl: './task-submitted.component.html',
  styleUrls: ['./task-submitted.component.scss']
})
export class TaskSubmittedComponent {
  constructor(private activeModal: NgbActiveModal) { }

  @Output() onReturnToTasksClick = new EventEmitter<void>();
  @Output() onStayOnTaskClick = new EventEmitter<void>();

  closeModal() {
    this.activeModal.close();
  }

  returnToTasksClick(): void {
    this.onReturnToTasksClick.emit();
  }

  stayOnTaskClick(): void {
    this.onStayOnTaskClick.emit();
  }
}

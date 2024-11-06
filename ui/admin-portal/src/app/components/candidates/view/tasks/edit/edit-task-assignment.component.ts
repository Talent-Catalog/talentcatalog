import {Component, Inject, LOCALE_ID, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {formatDate} from '@angular/common';
import {TaskAssignmentService, UpdateTaskAssignmentRequest} from "../../../../../services/task-assignment.service";
import {TaskAssignment} from "../../../../../model/task-assignment";
import {Task} from "../../../../../model/task";

@Component({
  selector: 'app-edit-task-assignment',
  templateUrl: './edit-task-assignment.component.html',
  styleUrls: ['./edit-task-assignment.component.scss']
})
export class EditTaskAssignmentComponent implements OnInit {

  taskAssignment: TaskAssignment;
  form: UntypedFormGroup;
  dueDate: string;
  date: Date;
  loading;
  saving;
  error;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private taskAssignmentService: TaskAssignmentService,
              @Inject(LOCALE_ID) private locale: string) { }

  ngOnInit(): void {
    this.loading = true;
    this.form = this.fb.group({
      dueDate: [this.formatTbbDate(this.taskAssignment?.dueDate)],
      complete: [this.isComplete]
    });
    this.loading = false;
  }

  formatTbbDate(date: Date): string {
    let d = null;
    if (date) {
      d = formatDate(date, "yyyy-MM-dd", this.locale);
    }
    return d;
  }

  get isComplete() {
    return this.taskAssignment.completedDate != null;
  }

  get isAbandoned() {
    return this.taskAssignment.abandonedDate != null;
  }

  onSave() {
    this.saving = true;

    const task: Task = this.form.value.task;

    //Pick up candidate and task
    const request: UpdateTaskAssignmentRequest = {
      taskAssignmentId: this.taskAssignment.id,
      dueDate: this.form.value.dueDate,
      completed: this.form.value.complete,
      abandoned: this.isAbandoned
    }

    this.taskAssignmentService.updateTaskAssignment(request).subscribe(
      (taskAssignment: TaskAssignment) => {
        this.activeModal.close(taskAssignment);
        this.saving = false;
      },
      error => {
        this.error = error;
        this.saving = false;
      }
    );
  }

  closeModal(ta: TaskAssignment) {
    this.activeModal.close(ta);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}

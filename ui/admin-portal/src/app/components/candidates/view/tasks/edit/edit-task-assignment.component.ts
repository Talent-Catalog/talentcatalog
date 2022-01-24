import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormGroup} from "@angular/forms";
import {TaskAssignment} from "../../../../../model/candidate";

@Component({
  selector: 'app-edit-task-assignment',
  templateUrl: './edit-task-assignment.component.html',
  styleUrls: ['./edit-task-assignment.component.scss']
})
export class EditTaskAssignmentComponent implements OnInit {

  taskAssignment: TaskAssignment;
  form: FormGroup;
  dueDate: string;
  date: Date;
  loading;
  saving;
  error;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder) { }

  ngOnInit(): void {
    this.loading = true;
    this.form = this.fb.group({
      dueDate: [this.formatDueDate(this.taskAssignment?.dueDate)],
      completedDate: [this.formatDueDate(this.taskAssignment?.completedDate)],
      complete: [this.isComplete]
    });
    this.loading = false;
  }

  formatDueDate(date: string) {
    let d: string;
    if (date != null) {
      d = date.slice(0, 10)
    } else {
      d = null;
    }
    return d;
  }

  get isComplete() {
    return this.taskAssignment.completedDate != null;
  }

  onSave() {
    // this.saving = true;
    // this.candidateService.update(this.candidateId, this.candidateForm.value).subscribe(
    //   (candidate) => {
    //     this.closeModal(candidate);
    //     this.saving = false;
    //   },
    //   (error) => {
    //     this.error = error;
    //     this.saving = false;
    //   });
  }

  closeModal(ta: TaskAssignment) {
    this.activeModal.close(ta);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}

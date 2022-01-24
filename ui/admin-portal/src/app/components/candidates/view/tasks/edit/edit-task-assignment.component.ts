import {Component, Inject, LOCALE_ID, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormGroup} from "@angular/forms";
import {TaskAssignment} from "../../../../../model/candidate";
import { formatDate } from '@angular/common';

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
              private fb: FormBuilder,
              @Inject(LOCALE_ID) private locale: string) { }

  ngOnInit(): void {
    this.loading = true;
    this.form = this.fb.group({
      abandonedDate: [this.formatTbbDate(this.taskAssignment?.abandonedDate)],
      dueDate: [this.formatTbbDate(this.taskAssignment?.dueDate)],
      completedDate: [this.formatTbbDate(this.taskAssignment?.completedDate)],
      complete: [this.isComplete]
    });
    this.loading = false;
  }

  formatTbbDate(date: Date): string {
    let d = null;
    if (date) {
      d = formatDate(this.taskAssignment?.dueDate, "yyyy-MM-dd", this.locale);
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

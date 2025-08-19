/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Candidate} from "../../../../../../model/candidate";
import {UntypedFormBuilder, UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {
  TaskAssignmentService,
  UpdateQuestionTaskAssignmentRequest,
  UpdateTaskAssignmentRequest,
  UpdateTaskCommentRequest,
  UpdateUploadTaskAssignmentRequest
} from "../../../../../../services/task-assignment.service";
import {DomSanitizer, SafeResourceUrl} from "@angular/platform-browser";
import {TaskAssignment} from "../../../../../../model/task-assignment";
import {TaskType} from "../../../../../../model/task";

@Component({
  selector: 'app-candidate-task',
  templateUrl: './candidate-task.component.html',
  styleUrls: ['./candidate-task.component.scss']
})
export class CandidateTaskComponent implements OnInit {
  @Input() selectedTask: TaskAssignment;
  @Input() candidate: Candidate;
  @Output() back = new EventEmitter();
  form: UntypedFormGroup;
  commentForm: UntypedFormGroup;

  url: SafeResourceUrl;
  loading: boolean;
  saving: boolean;
  error;
  errorMessage: string | null = null;
  discrepantFields: Set<string> = new Set();

  constructor(private fb: UntypedFormBuilder,
              private taskAssignmentService: TaskAssignmentService,
              public sanitizer: DomSanitizer) { }

  ngOnInit(): void {
    this.form = this.fb.group({});
    this.commentForm = this.fb.group({
      abandoned: [this.abandonedTask],
      comment: [this.selectedTask.candidateNotes]
    })
    this.addRequiredFormControls();

    if (this.selectedTask.task.docLink) {
      this.url = this.sanitizer.bypassSecurityTrustResourceUrl(this.selectedTask?.task?.docLink);
    }

    // Validation requiring comment if abandoned, and resetting the required validation on the answer/completed fields.
    // subscribe abandoned logic only on commentForm
    this.commentForm.get('abandoned').valueChanges.subscribe(abandoned => {
      if (abandoned) {
        this.commentForm.get('comment').setValidators([Validators.required]);
      } else {
        this.commentForm.get('comment').clearValidators();
      }
      this.commentForm.get('comment').updateValueAndValidity();
    });
  }

  addRequiredFormControls() {
    if (!this.formAbandoned) {
      if (this.selectedTask.task.taskType === 'Question' || this.selectedTask.task.taskType === 'YesNoQuestion') {
        this.form.addControl('response', new UntypedFormControl(this.selectedTask?.answer ? this.answer : null, Validators.required));
      } else if (this.selectedTask.task.taskType === 'Simple') {
        this.form.addControl('completed', new UntypedFormControl({value: this.completedTask,
          disabled: this.completedTask}, Validators.requiredTrue));
      }
    }
  }

  get formAbandoned() {
    return this.commentForm.get('abandoned').value;
  }

  get abandonedTask() {
    return this.selectedTask?.abandonedDate != null;
  }

  get completedTask() {
    return this.selectedTask?.completedDate != null;
  }

  get answer() {
    let a: string;
    if (this.selectedTask?.task?.allowedAnswers != null) {
      a = this.selectedTask?.task?.allowedAnswers.find(value => value.displayName === this.selectedTask?.answer).name;
    } else {
      a = this.selectedTask?.answer;
    }
    return a;
  }

  goBack() {
    this.selectedTask = null;
    this.back.emit();
  }

  isOverdue(ta: TaskAssignment) {
    return (new Date(ta.dueDate) < new Date()) && !ta.task.optional;
  }

  completedUploadTask($event: TaskAssignment) {
    this.selectedTask = $event;
  }

  submitTask() {
    // This handles the submission of the non upload tasks, including any comment or if abandoned.
    // If it is an upload task the task is completed separately on file upload, the submit button will then add a comment or if abandoned to the upload task.
    // If it is an already completed task, only can update the comment field.
    if (this.completedTask) {
      this.updateTaskComment();
    } else {
      if (!this.abandonedTask) {
        if (this.selectedTask.task.taskType === TaskType.Question || this.selectedTask.task.taskType === TaskType.YesNoQuestion) {
          this.updateQuestionTask();
        } else if (this.selectedTask.task.taskType === TaskType.Simple) {
          this.updateSimpleTask();
        } else {
          console.log("Upload")
          this.updateUploadTask();
        }
      } else {
        this.updateAbandonedTask();
      }
    }
  }

  updateQuestionTask() {
    this.saving = true;
    const request: UpdateQuestionTaskAssignmentRequest = {
      answer: this.form.value.response,
      abandoned: this.commentForm.value.abandoned,
      candidateNotes: this.commentForm.value.comment
    }
    this.taskAssignmentService.updateQuestionTask(this.selectedTask.id, request).subscribe(
      (taskAssignment) => {
        this.selectedTask = taskAssignment;
        this.saving = false;
      }, error => {
        this.error = error;
        this.saving = false;
      }
    )
  }

  updateSimpleTask() {
    this.saving = true;
    const request: UpdateTaskAssignmentRequest = {
      completed: this.form.value.completed,
      abandoned: this.commentForm.value.abandoned,
      candidateNotes: this.commentForm.value.comment
    }
    this.taskAssignmentService.updateTaskAssignment(this.selectedTask.id, request).subscribe(
      (taskAssignment) => {
        this.selectedTask = taskAssignment;
        this.saving = false;
      }, error => {
        this.error = error;
        this.saving = false;
      }
    )
  }

  // This is an update of a task assignment of only the comment/abandoned field.
  // It is used for upload tasks, as the upload task is completed separately upon file upload.
  updateUploadTask() {
    this.saving = true;
    const request: UpdateUploadTaskAssignmentRequest = {
      abandoned: this.commentForm.value.abandoned,
      candidateNotes: this.commentForm.value.comment
    }
    console.log(request)
    this.taskAssignmentService.updateUploadTaskAssignment(this.selectedTask.id, request).subscribe(
      (taskAssignment) => {
        this.selectedTask = taskAssignment;
        this.saving = false;
      }, error => {
        this.error = error;
        this.saving = false;
      }
    )
  }

  // Handle errors from ViewUploadTaskComponent
  handleUploadError(event: { message: string, discrepantFields: Set<string> }) {
    this.errorMessage = event.message;
    this.discrepantFields = event.discrepantFields;
  }

  // If we want to update an abandoned task, the completed field is false and we are only updating the abandoned/comment field.
  updateAbandonedTask() {
    console.log("AS")
    this.saving = true;
    const request: UpdateTaskAssignmentRequest = {
      completed: false,
      abandoned: this.commentForm.value.abandoned,
      candidateNotes: this.commentForm.value.comment
    }
    console.log(request)
    this.taskAssignmentService.updateTaskAssignment(this.selectedTask.id, request).subscribe(
      (taskAssignment) => {
        this.selectedTask = taskAssignment;
        this.addRequiredFormControls();
        this.saving = false;
      }, error => {
        this.error = error;
        this.saving = false;
      }
    )
  }

  updateTaskComment() {
    this.saving = true;
    const request: UpdateTaskCommentRequest = {
      candidateNotes: this.commentForm.value.comment
    }
    this.taskAssignmentService.updateTaskComment(this.selectedTask.id, request).subscribe(
      (taskAssignment) => {
        this.selectedTask.candidateNotes = taskAssignment.candidateNotes;
        this.saving = false;
      }, error => {
        this.error = error;
        this.saving = false;
      }
    )
  }
}

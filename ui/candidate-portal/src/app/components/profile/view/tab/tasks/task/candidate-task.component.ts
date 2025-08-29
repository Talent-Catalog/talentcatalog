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
  url: SafeResourceUrl;
  loading: boolean;
  saving: boolean;
  error;

  constructor(private fb: UntypedFormBuilder,
              private taskAssignmentService: TaskAssignmentService,
              public sanitizer: DomSanitizer) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      abandoned: [this.abandonedTask],
      comment: [this.selectedTask.candidateNotes]
    })

    this.addRequiredFormControls();

    if (this.selectedTask.task.docLink) {
      this.url = this.sanitizer.bypassSecurityTrustResourceUrl(this.selectedTask?.task?.docLink);
    }

    // todo this validation seems very messy! May be a better way to handle this. Perhaps use seperate forms?
    // Validation requiring comment if abandoned, and resetting the required validation on the answer/completed fields.
    this.form.get('abandoned').valueChanges.subscribe(abandoned => {
      if (abandoned) {
        this.form.get('comment').setValidators([Validators.required]);
        this.form.get('response')?.clearValidators();
        this.form.get('completed')?.clearValidators();
      } else {
        // If task isn't already abandoned, and the abandon checkbox is false. Set required validators. But don't set
        // these required validators if we are unchecking an already abandoned task.
        if (this.selectedTask.abandonedDate == null) {
          this.form.get('response')?.setValidators([Validators.required]);
          this.form.get('completed')?.setValidators([Validators.requiredTrue]);
        } else {
          this.form.get('response')?.clearValidators();
          this.form.get('completed')?.clearValidators();
        }
        this.form.get('comment').clearValidators();
      }
      this.form.controls['comment'].updateValueAndValidity()
      this.form.controls['response']?.updateValueAndValidity()
      this.form.controls['completed']?.updateValueAndValidity()
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
    return this.form.get('abandoned').value;
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
        switch (this.selectedTask.task.taskType) {
          case TaskType.Form:
            this.updateFormTask();
            break;
          case TaskType.Question:
          case TaskType.YesNoQuestion:
            this.updateQuestionTask();
            break;
          case TaskType.Simple:
            this.updateSimpleTask();
            break;
          case TaskType.Upload:
            this.updateUploadTask();
            break;
        }
      } else {
        this.updateAbandonedTask();
      }
    }
  }

  updateFormTask() {
    //todo
  }

  updateQuestionTask() {
    this.saving = true;
    const request: UpdateQuestionTaskAssignmentRequest = {
      answer: this.form.value.response,
      abandoned: this.form.value.abandoned,
      candidateNotes: this.form.value.comment
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
      abandoned: this.form.value.abandoned,
      candidateNotes: this.form.value.comment
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
      abandoned: this.form.value.abandoned,
      candidateNotes: this.form.value.comment
    }
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

  // If we want to update an abandoned task, the completed field is false and we are only updating the abandoned/comment field.
  updateAbandonedTask() {
    this.saving = true;
    const request: UpdateTaskAssignmentRequest = {
      completed: false,
      abandoned: this.form.value.abandoned,
      candidateNotes: this.form.value.comment
    }
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
      candidateNotes: this.form.value.comment
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

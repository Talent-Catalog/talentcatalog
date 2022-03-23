/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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
import {Candidate, TaskAssignment, TaskType} from "../../../../../../model/candidate";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {
  TaskAssignmentService,
  UpdateQuestionTaskAssignmentRequest,
  UpdateTaskAssignmentRequest,
  UpdateUploadTaskAssignmentRequest
} from "../../../../../../services/task-assignment.service";
import {DomSanitizer} from "@angular/platform-browser";

@Component({
  selector: 'app-candidate-task',
  templateUrl: './candidate-task.component.html',
  styleUrls: ['./candidate-task.component.scss']
})
export class CandidateTaskComponent implements OnInit {
  @Input() selectedTask: TaskAssignment;
  @Input() candidate: Candidate;
  @Output() back = new EventEmitter();
  form: FormGroup;
  loading: boolean;
  saving: boolean;
  error;

  constructor(private fb: FormBuilder,
              private taskAssignmentService: TaskAssignmentService,
              public sanitizer: DomSanitizer) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      abandoned: [this.isAbandoned],
      comment: [this.selectedTask.candidateNotes]
    })

    this.addRequiredFormControls();

    // todo this validation seems very messy! Must be a better way to handle this.
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
    if (!this.isAbandoned) {
      if (this.selectedTask.task.taskType === 'Question' || this.selectedTask.task.taskType === 'YesNoQuestion') {
        this.form.addControl('response', new FormControl(this.selectedTask?.answer, Validators.required));
      } else if (this.selectedTask.task.taskType === 'Simple') {
        this.form.addControl('completed', new FormControl({value: this.isComplete,
          disabled: this.selectedTask.completedDate != null}, Validators.requiredTrue));
      }
    }
  }

  get isAbandoned() {
    return this.selectedTask.abandonedDate != null;
  }

  get isComplete() {
    return this.selectedTask.completedDate != null;
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
    if (!this.isAbandoned) {
      if (this.selectedTask.task.taskType === TaskType.Question || this.selectedTask.task.taskType === TaskType.YesNoQuestion) {
        this.updateQuestionTask();
      } else if (this.selectedTask.task.taskType === TaskType.Simple) {
        this.updateSimpleTask();
      } else {
        this.updateUploadTask();
      }
    } else {
      this.updateAbandonedTask();
    }
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

}

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
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {
  TaskAssignmentService,
  UpdateQuestionTaskRequest,
  UpdateSimpleTaskRequest,
  UpdateTaskAssignmentRequest
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
      response: [null, Validators.required],
      abandoned: [this.isAbandoned],
      comment: [this.selectedTask.candidateNotes]
    })

    // Set comment as required field if abandon is checked
    this.form.get('abandoned').valueChanges.subscribe(abandoned => {
      if (abandoned) {
        this.form.get('comment').setValidators([Validators.required]);
        this.form.get('response').clearValidators();
      } else {
        this.form.get('comment').clearValidators();
        this.form.get('response').setValidators([Validators.required]);
      }
      this.form.controls['comment'].updateValueAndValidity()
      this.form.controls['response'].updateValueAndValidity()
    });
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
    if (this.selectedTask.task.taskType === TaskType.Question || this.selectedTask.task.taskType === TaskType.YesNoQuestion) {
      this.updateQuestionTask();
    } else if (this.selectedTask.task.taskType === TaskType.Simple) {
      this.updateSimpleTask();
    } else {
      this.updateUploadTask();
    }
  }

  updateQuestionTask() {
    this.saving = true;
    const request: UpdateQuestionTaskRequest = {
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
    const request: UpdateSimpleTaskRequest = {
      completed: this.form.value.response,
      abandoned: this.form.value.abandoned,
      candidateNotes: this.form.value.comment
    }
    this.taskAssignmentService.updateSimpleTask(this.selectedTask.id, request).subscribe(
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
    const request: UpdateTaskAssignmentRequest = {
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

}

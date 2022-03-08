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
import {forkJoin, Observable} from "rxjs";
import {CandidateAttachment} from "../../../../../../model/candidate-attachment";
import {
  TaskAssignmentService,
  UpdateQuestionTaskRequest,
  UpdateTaskAssignmentRequest
} from "../../../../../../services/task-assignment.service";
import {DomSanitizer, SafeResourceUrl} from "@angular/platform-browser";

@Component({
  selector: 'app-candidate-task',
  templateUrl: './candidate-task.component.html',
  styleUrls: ['./candidate-task.component.scss']
})
export class CandidateTaskComponent implements OnInit {
  @Input() selectedTask: TaskAssignment;
  @Input() candidate: Candidate;
  @Output() back = new EventEmitter();
  filesUploaded: File[];
  form: FormGroup;
  loading: boolean;
  uploading: boolean;
  saving: boolean;
  error;
  url: SafeResourceUrl;

  constructor(private fb: FormBuilder,
              private taskAssignmentService: TaskAssignmentService,
              public sanitizer: DomSanitizer) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      completeSimple: [null],
      completeQuestion: [null],
      completeYNQuestion: [null],
      comment: [this.selectedTask.candidateNotes],
      abandoned: [this.isAbandoned]
    })

    // Set comment as required field if abandon is checked
    this.form.get('abandoned').valueChanges.subscribe(abandoned => {
      if (abandoned) {
        this.form.get('comment').setValidators([Validators.required])
      } else {
        this.form.get('comment').clearValidators();
      }
      this.form.controls['comment'].updateValueAndValidity()
    });

    this.url = this.sanitizer.bypassSecurityTrustResourceUrl(this.selectedTask?.task?.helpLink);
  }

  get isAbandoned() {
    return this.selectedTask.abandonedDate != null;
  }

  get isComplete() {
    return this.selectedTask.completedDate != null;
  }

  completeUploadTask($event) {
    this.error = null;
    this.uploading = true;

    //todo this all doesn't look right - needs work.
    const uploads: Observable<TaskAssignment>[] = [];
    for (const file of $event.files) {
      const formData: FormData = new FormData();
      formData.append('file', file);

      this.taskAssignmentService.completeUploadTask(this.selectedTask.id, formData).subscribe(
        (taskAssignment: TaskAssignment) => {
          this.selectedTask = taskAssignment;
          // This allows us to display the success message in the html
          this.filesUploaded = $event.files;
          this.uploading = false;
        },
        error => {
          this.error = error;
          this.uploading = false;
        }
      );
    }

    forkJoin(...uploads).subscribe(
      (results: CandidateAttachment[]) => {
        this.uploading = false;
      },
      error => {
        this.error = error;
        this.uploading = false;
      }
    );
  }

  getFileName(fileName: string): string {
    return this.candidate?.candidateNumber + "-" + this.selectedTask?.task?.uploadType + "-" + fileName;
  }

  goBack() {
    this.selectedTask = null;
    this.back.emit();
  }

  isOverdue(ta: TaskAssignment) {
    return (new Date(ta.dueDate) < new Date()) && !ta.task.optional;
  }

  submitTask() {
    // This handles the submission of the non upload tasks, including any comment or if abandoned.
    // If it is an upload task the task is completed separately on file upload, the submit button will then add a comment or if abandoned to the upload task.
    if (this.selectedTask.task.taskType === TaskType.Question || this.selectedTask.task.taskType === TaskType.YesNoQuestion) {
      this.completeQuestionTask();
    } else if (this.selectedTask.task.taskType === TaskType.Simple) {
      this.completeSimpleTask();
    } else {
      this.addUploadTaskComment();
    }
  }

  completeQuestionTask() {
    this.saving = true;
    const request: UpdateQuestionTaskRequest = {
      taskAssignmentId: this.selectedTask.id,
      answer: this.form.value.completeQuestion,
      abandoned: this.form.value.abandoned,
      completed: this.isComplete,
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

  completeSimpleTask() {
    this.saving = true;
    const request: UpdateTaskAssignmentRequest = {
      taskAssignmentId: this.selectedTask.id,
      completed: this.form.value.completeSimple,
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

  addUploadTaskComment() {
    this.saving = true;
    const request: UpdateTaskAssignmentRequest = {
      taskAssignmentId: this.selectedTask.id,
      completed: this.isComplete,
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

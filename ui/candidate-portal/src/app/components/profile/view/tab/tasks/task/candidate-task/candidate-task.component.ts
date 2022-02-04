import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Candidate, TaskAssignment, TaskType} from "../../../../../../../model/candidate";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {forkJoin, Observable} from "rxjs";
import {CandidateAttachment} from "../../../../../../../model/candidate-attachment";
import {
  CompleteQuestionTaskRequest,
  CompleteSimpleTaskRequest,
  TaskAssignmentService,
  UpdateTaskAssignmentRequest
} from "../../../../../../../services/task-assignment.service";

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
  commentForm: FormGroup;
  completeForm: FormGroup;
  loading: boolean;
  uploading: boolean;
  saving: boolean;
  error;

  constructor(private fb: FormBuilder,
              private taskAssignmentService: TaskAssignmentService) { }

  ngOnInit(): void {
    this.completeForm = this.fb.group({
      completeSimple: [null],
      completeQuestion: [null],
      completeYNQuestion: [null]
    })

    this.commentForm = this.fb.group({
      comment: [this.selectedTask.candidateNotes],
      abandoned: [this.isAbandoned]
    });

    // Set comment as required field if abandon is checked
    this.commentForm.get('abandoned').valueChanges.subscribe(abandoned => {
      if (abandoned) {
        this.commentForm.get('comment').setValidators([Validators.required])
      } else {
        this.commentForm.get('comment').clearValidators();
      }
      this.commentForm.controls['comment'].updateValueAndValidity()
    });
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


  completeNonUploadTask() {
    if (this.selectedTask.task.taskType === TaskType.Question) {
      this.completeQuestionTask();
    } else if (this.selectedTask.task.taskType === TaskType.YesNoQuestion) {
      this.completeYNQuestionTask();
    } else {
      this.completeSimpleTask();
    }
  }

  completeSimpleTask() {
    this.saving = true;
    const req: CompleteSimpleTaskRequest = {
      completed: this.completeForm.value.completeSimple,
    }
    this.taskAssignmentService.completeSimpleTask(this.selectedTask.id, req).subscribe(
      (taskAssignment) => {
        this.selectedTask = taskAssignment;
        this.saving = false;
      }, error => {
        this.error = error;
        this.saving = false;
      }
    )
  }

  completeQuestionTask() {
    this.saving = true;
    const req: CompleteQuestionTaskRequest = {
      answer: this.completeForm.value.completeQuestion,
    }
    this.taskAssignmentService.completeQuestionTask(this.selectedTask.id, req).subscribe(
      (taskAssignment) => {
        this.selectedTask = taskAssignment;
        this.saving = false;
      }, error => {
        this.error = error;
        this.saving = false;
      }
    )
  }

  completeYNQuestionTask() {
    this.saving = true;
    const req: CompleteQuestionTaskRequest = {
      answer: this.completeForm.value.completeYNQuestion,
    }
    this.taskAssignmentService.completeYNQuestionTask(this.selectedTask.id, req).subscribe(
      (taskAssignment) => {
        this.selectedTask = taskAssignment;
        this.saving = false;
      }, error => {
        this.error = error;
        this.saving = false;
      }
    )
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

  submitComment() {
    this.saving = true;
    const updateRequest: UpdateTaskAssignmentRequest = {
      taskAssignmentId: this.selectedTask.id,
      candidateNotes: this.commentForm.value.comment,
      abandoned: this.commentForm.value.abandoned,
      complete: this.isComplete
    }
    this.taskAssignmentService.update(this.selectedTask.id, updateRequest).subscribe(
      (taskAssignment: TaskAssignment) => {
        this.selectedTask = taskAssignment;
        this.saving = false;
      }, error => {
        this.error = error;
        this.saving = false;
      }
    )
  }

}

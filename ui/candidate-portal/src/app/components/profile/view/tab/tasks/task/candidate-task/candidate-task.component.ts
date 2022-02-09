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
  form: FormGroup;
  loading: boolean;
  uploading: boolean;
  saving: boolean;
  error;

  constructor(private fb: FormBuilder,
              private taskAssignmentService: TaskAssignmentService) { }

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
    this.completeNonUploadTask();
    this.addComment();
  }

  // This handles the API call to send the candidate's comment, as well as if the task is abandoned.
  addComment() {
    this.saving = true;
    const updateRequest: UpdateTaskAssignmentRequest = {
      taskAssignmentId: this.selectedTask.id,
      candidateNotes: this.form.value.comment,
      abandoned: this.form.value.abandoned,
      completed: this.isComplete
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

  // This handles the submission of the non upload task, and the relevant API call appropriate to the task type.
  // If it is an upload task, it will not call any API as upload tasks are completed seperately.
  completeNonUploadTask() {
    if (this.selectedTask.task.taskType === TaskType.Question) {
      this.completeQuestionTask();
    } else if (this.selectedTask.task.taskType === TaskType.YesNoQuestion) {
      this.completeYNQuestionTask();
    } else if (this.selectedTask.task.taskType === TaskType.Simple) {
      this.completeSimpleTask();
    }
  }

  completeSimpleTask() {
    this.saving = true;
    const req: CompleteSimpleTaskRequest = {
      completed: this.form.value.completeSimple,
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
      answer: this.form.value.completeQuestion,
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
      answer: this.form.value.completeYNQuestion,
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

}

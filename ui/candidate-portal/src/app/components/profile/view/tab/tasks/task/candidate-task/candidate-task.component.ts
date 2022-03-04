import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Candidate, TaskAssignment} from "../../../../../../../model/candidate";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {forkJoin, Observable} from "rxjs";
import {CandidateAttachment} from "../../../../../../../model/candidate-attachment";
import {
  TaskAssignmentService,
  UpdateTaskAssignmentRequest
} from "../../../../../../../services/task-assignment.service";
import {DomSanitizer, SafeResourceUrl,} from '@angular/platform-browser';

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
      comment: [this.selectedTask.candidateNotes],
      abandoned: [this.isAbandoned]
    });

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

  startServerUpload($event) {
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

  submitComment() {
    this.saving = true;
    const updateRequest: UpdateTaskAssignmentRequest = {
      taskAssignmentId: this.selectedTask.id,
      candidateNotes: this.form.value.comment,
      abandoned: this.form.value.abandoned,
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

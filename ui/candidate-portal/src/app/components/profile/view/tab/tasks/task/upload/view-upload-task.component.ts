import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {forkJoin, Observable} from "rxjs";
import {Candidate} from "../../../../../../../model/candidate";
import {CandidateAttachment} from "../../../../../../../model/candidate-attachment";
import {UntypedFormGroup} from "@angular/forms";
import {TaskAssignmentService} from "../../../../../../../services/task-assignment.service";
import {TaskAssignment} from "../../../../../../../model/task-assignment";

@Component({
  selector: 'app-view-upload-task',
  templateUrl: './view-upload-task.component.html',
  styleUrls: ['./view-upload-task.component.scss']
})
export class ViewUploadTaskComponent implements OnInit {
  @Input() form: UntypedFormGroup;
  @Input() selectedTask: TaskAssignment;
  @Input() candidate: Candidate;
  @Output() successfulUpload = new EventEmitter<TaskAssignment>();
  filesUploaded: File[];
  loading: boolean;
  uploading: boolean;
  saving: boolean;
  error;

  constructor(private taskAssignmentService: TaskAssignmentService) { }

  ngOnInit(): void {
  }

  completeUploadTask($event) {
    this.error = null;
    this.uploading = true;

    //todo this all doesn't look right - needs work.
    const uploads: Observable<TaskAssignment>[] = [];
    for (const file of $event.files) {
      const formData: FormData = new FormData();
      formData.append('file', file);

      this.taskAssignmentService.doUploadTask(this.selectedTask.id, formData).subscribe(
        (taskAssignment: TaskAssignment) => {
          this.successfulUpload.emit(taskAssignment);
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

}

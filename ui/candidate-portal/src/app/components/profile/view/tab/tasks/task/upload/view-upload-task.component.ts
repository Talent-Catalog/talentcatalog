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
import {forkJoin, Observable} from "rxjs";
import {Candidate} from "../../../../../../../model/candidate";
import {UntypedFormGroup} from "@angular/forms";
import {TaskAssignmentService} from "../../../../../../../services/task-assignment.service";
import {TaskAssignment} from "../../../../../../../model/task-assignment";
import {MetadataField} from "../../../../../../../model/task";

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
  @Output() errorWithDiscrepancies = new EventEmitter<{ message: string, discrepantFields: Set<string> }>();
  filesUploaded: File[];
  loading: boolean;
  uploading: boolean;
  saving: boolean;
  error;

  constructor(private taskAssignmentService: TaskAssignmentService) { }

  ngOnInit(): void {
  }

  completeUploadTask($event: { files: File[], type: string }) {
    this.error = null;
    this.uploading = true;

    if (this.selectedTask.task.name === 'candidateTravelDocumentUpload') {
      // Validate metadata for candidateTravelDocumentUpload
      const metadata = this.extractMetadata();
      const requiredFields = this.selectedTask.task.requiredMetadata as MetadataField[];
      const missingFields = requiredFields.filter(field => !metadata[field.name]);
      if (missingFields.length > 0) {
        this.error = {
          message: 'ERRORS.MISSING_FIELDS',
          params: { fields: missingFields.map(f => f.label).join(', ') }
        };
        this.uploading = false;
        return;
      }

      if ($event.files.length === 0) {
        this.error = { message: 'ERRORS.NO_FILES_UPLOADED' };
        this.uploading = false;
        return;
      }

      // Send files and metadata to complete-upload-task endpoint
      const formData = new FormData();
      $event.files.forEach((file, index) => {
        formData.append('file', file, file.name);
      });
      formData.append('fieldAnswers', JSON.stringify(metadata));

      this.taskAssignmentService.completeUploadTask(this.selectedTask.id, formData).subscribe(
        (taskAssignment: TaskAssignment) => {
          this.successfulUpload.emit(taskAssignment);
          this.filesUploaded = $event.files;
          this.uploading = false;
        },
        error => {
          this.uploading = false;

          const backendMessage = error || 'ERRORS.UPLOAD_FAILED';

          // Make sure discrepancies is always an array
          const discrepancies: string[] = Array.isArray(error) ? error : [error];

          const discrepantFields: Set<string> = new Set();
          discrepancies.forEach(msg => {
            if (msg.includes('First name')) discrepantFields.add('firstName');
            if (msg.includes('Last name')) discrepantFields.add('lastName');
            if (msg.includes('Date of birth')) discrepantFields.add('dob');
            if (msg.includes('Gender')) discrepantFields.add('gender');
            if (msg.includes('Country of birth')) discrepantFields.add('birthCountry');
          });

          this.error = { message: backendMessage, discrepancies, discrepantFields };
          this.errorWithDiscrepancies.emit({ message: backendMessage, discrepantFields });

          discrepantFields.forEach(fieldName => {
            const control = this.form.get(fieldName);
            if (control) {
              control.setErrors({ discrepancy: true });
              control.markAsTouched();
            }
          });
        }

      );
    } else {
      // Handle other upload tasks using doUploadTask
      const uploads: Observable<TaskAssignment>[] = [];
      for (const file of $event.files) {
        const formData = new FormData();
        formData.append('file', file);
        uploads.push(this.taskAssignmentService.doUploadTask(this.selectedTask.id, formData));
      }

      forkJoin(uploads).subscribe(
        (results: TaskAssignment[]) => {
          // Emit the last TaskAssignment (assuming single file for simplicity, adjust if multiple needed)
          this.successfulUpload.emit(results[results.length - 1]);
          this.filesUploaded = $event.files;
          this.uploading = false;
        },
        error => {
          this.error = error;
          this.uploading = false;
        }
      );
    }
  }

  getFileName(fileName: string): string {
    return this.candidate?.candidateNumber + "-" + this.selectedTask?.task?.uploadType + "-" + fileName;
  }

  private extractMetadata(): { [key: string]: string } {
    const metadata: { [key: string]: string } = {};
    if (this.selectedTask.task.requiredMetadata) {
      try {
        const metadataFields = this.selectedTask.task.requiredMetadata as MetadataField[];
        metadataFields.forEach((field: MetadataField) => {
          const value = this.form.get(field.name)?.value;
          if (value) {
            metadata[field.name] = value;
          }
        });
      } catch (error) {
        console.error('Failed to parse requiredMetadata:', error);
      }
    }
    return metadata;
  }
}

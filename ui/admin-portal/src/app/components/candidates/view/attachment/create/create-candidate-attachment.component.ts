import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormGroup} from "@angular/forms";
import {S3UploadParams} from "../../../../../model/s3-upload-params";
import {CandidateAttachmentService} from "../../../../../services/candidate-attachment.service";
import {CandidateAttachment} from "../../../../../model/candidate-attachment";

@Component({
  selector: 'app-create-candidate-attachment',
  templateUrl: './create-candidate-attachment.component.html',
  styleUrls: ['./create-candidate-attachment.component.scss']
})
export class CreateCandidateAttachmentComponent implements OnInit {

  error: any;
  saving: boolean;

  // Set in the parent component, by referencing the comoponent instance
  candidateId: number;
  type: string;

  form: FormGroup;
  attachments: CandidateAttachment[];

  constructor(private modal: NgbActiveModal,
              private candidateAttachmentService: CandidateAttachmentService,
              private fb: FormBuilder) { }

  ngOnInit() {
    this.attachments = [];

    this.form = this.fb.group({
      candidateId: [this.candidateId],
      type: [this.type],
      location: [''],
      name: ['']
    });
  }

  cancel() {
    this.modal.close();
  }

  close() {
    this.modal.close();
  }

  handleAttachmentUploaded(attachment: {s3Params: S3UploadParams, file: File}) {
    const request = {
      candidateId: this.candidateId,
      type: 'file',
      name: attachment.file.name,
      fileType: this.getFileType(attachment.file.name),
      folder: attachment.s3Params.objectKey
    };
    this.candidateAttachmentService.createAttachment(request).subscribe(
      (response) => this.attachments.push(response),
      (error) => this.error = error
    );
  }

  getFileType(name: string) {
    const fragments = name.split(".");
    if (fragments.length > 1) {
      return fragments[fragments.length - 1];
    }
    return '';
  }

  save() {
    this.candidateAttachmentService.createAttachment(this.form.value).subscribe(
      (response) => this.modal.close(),
      (error) => this.error = error
    );
  }

}

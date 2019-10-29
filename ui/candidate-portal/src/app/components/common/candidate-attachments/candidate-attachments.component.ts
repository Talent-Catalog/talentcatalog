import {Component, Input, OnInit} from '@angular/core';
import {CandidateAttachmentService} from "../../../services/candidate-attachment.service";
import {AttachmentType, CandidateAttachment} from "../../../model/candidate-attachment";
import {FormBuilder, FormGroup} from "@angular/forms";
import {S3UploadParams} from "../../../model/s3-upload-params";
import {environment} from "../../../../environments/environment";

@Component({
  selector: 'app-candidate-attachments',
  templateUrl: './candidate-attachments.component.html',
  styleUrls: ['./candidate-attachments.component.scss']
})
export class CandidateAttachmentsComponent implements OnInit {

  @Input() preview: boolean = false;

  error: any;
  loading: boolean = true;
  deleting: boolean;

  form: FormGroup;
  attachments: CandidateAttachment[] = [];
  s3BucketUrl: string = environment.s3BucketUrl;

  constructor(private fb: FormBuilder,
              private candidateAttachmentService: CandidateAttachmentService) { }

  ngOnInit() {
    this.candidateAttachmentService.listCandidateAttachments().subscribe(
      (response) => {
        this.attachments = response;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

  handleAttachmentUploaded(attachment: {s3Params: S3UploadParams, file: File}) {
    const request = {
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
    return ''
  }

  getAttachmentUrl(attachment: CandidateAttachment) {
    let candidateNumber = 'CN20001';
    if (attachment.type === AttachmentType.file) {
      return this.s3BucketUrl + '/candidate/' + candidateNumber + '/' + attachment.name;
    }
    return
  }

  deleteAttachment(attachment: CandidateAttachment) {
    this.deleting = true;
    this.candidateAttachmentService.deleteAttachment(attachment.id).subscribe(
      (response) => {
        this.attachments = this.attachments.filter(att => att.name !== attachment.name);
        this.deleting = false;
      },
      (error) => {
        console.log('error', error);
      });
  }
}

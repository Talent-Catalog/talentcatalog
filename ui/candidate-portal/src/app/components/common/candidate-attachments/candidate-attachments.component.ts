import {Component, Input, OnInit} from '@angular/core';
import {CandidateAttachmentService} from "../../../services/candidate-attachment.service";
import {AttachmentType, CandidateAttachment} from "../../../model/candidate-attachment";
import {FormBuilder, FormGroup} from "@angular/forms";
import {S3UploadParams} from "../../../model/s3-upload-params";
import {environment} from "../../../../environments/environment";
import {CandidateService} from "../../../services/candidate.service";

@Component({
  selector: 'app-candidate-attachments',
  templateUrl: './candidate-attachments.component.html',
  styleUrls: ['./candidate-attachments.component.scss']
})
export class CandidateAttachmentsComponent implements OnInit {

  @Input() preview: boolean = false;

  error: any;
  _loading = {
    candidate: true,
    attachments: true
  };
  deleting: boolean;

  s3BucketUrl: string = environment.s3BucketUrl;
  form: FormGroup;

  attachments: CandidateAttachment[] = [];
  candidateNumber: string;

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService,
              private candidateAttachmentService: CandidateAttachmentService) { }

  ngOnInit() {
    this.candidateService.getCandidateNumber().subscribe(
      (response) => {
        this.candidateNumber = response.candidateNumber;
        this._loading.candidate = false;
      },
      (error) => {
        this.error = error;
        this._loading.candidate = false;
      });
    this.candidateAttachmentService.listCandidateAttachments().subscribe(
      (response) => {
        this.attachments = response;
        this._loading.attachments = false;
      },
      (error) => {
        this.error = error;
        this._loading.attachments = false;
      });
  }

  get loading() {
    const l = this._loading;
    return l.attachments || l.candidate;
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
    if (attachment.type === AttachmentType.file) {
      return this.s3BucketUrl + '/candidate/' + this.candidateNumber + '/' + attachment.name;
    }
    return attachment.location;
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

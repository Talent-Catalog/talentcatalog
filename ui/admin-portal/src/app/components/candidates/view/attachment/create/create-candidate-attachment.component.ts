import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormGroup} from "@angular/forms";
import {S3UploadParams} from "../../../../../model/s3-upload-params";
import {CandidateAttachmentService} from "../../../../../services/candidate-attachment.service";
import {
  AttachmentType,
  CandidateAttachment,
  CandidateAttachmentRequest
} from "../../../../../model/candidate-attachment";

@Component({
  selector: 'app-create-candidate-attachment',
  templateUrl: './create-candidate-attachment.component.html',
  styleUrls: ['./create-candidate-attachment.component.scss']
})
export class CreateCandidateAttachmentComponent implements OnInit {

  error: any;
  saving: boolean;

  // Set in the parent component, by referencing the component instance
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
      name: [''],
      cv: [false]
    });
  }

  cancel() {
    this.modal.close();
  }

  close() {
    this.modal.close();
  }

  // For file attachment
  handleAttachmentUploaded(attachment: {s3Params: S3UploadParams, file: File}) {
    const request: CandidateAttachmentRequest = new CandidateAttachmentRequest();
    request.candidateId = this.candidateId;
    request.type = AttachmentType.googlefile;
    request.name = attachment.file.name;
    request.fileType = this.getFileType(attachment.file.name);
    request.cv = this.form.value.cv;
    request.folder = attachment.s3Params.objectKey;
    this.candidateAttachmentService.createAttachment(request).subscribe(
      (response) => this.attachments.push(response),
      (error) => this.error = error
    );
  }

  /**
   * Returns suffix of file name - eg pdf
   * @param name File name
   */
  getFileType(name: string): string {
    const fragments = name.split(".");
    if (fragments.length > 1) {
      return fragments[fragments.length - 1];
    }
    return '';
  }

  // For link attachment
  save() {
    const request: CandidateAttachmentRequest = new CandidateAttachmentRequest();
    request.candidateId = this.candidateId;
    request.type = this.form.value.type;
    request.name = this.form.value.name;
    request.location = this.form.value.location;
    request.cv = false;
    this.candidateAttachmentService.createAttachment(request).subscribe(
      (response) => this.modal.close(),
      (error) => this.error = error
    );
  }

  startServerUpload(files: File[]) {
    this.saving = true;

    //todo Handle multiple files
    const file: File = files[0];

    const cv: boolean = this.form.value.cv;
    const formData: FormData = new FormData();
    formData.append('file', file);

    //Upload file to server
    this.candidateAttachmentService.uploadAttachment(this.candidateId, cv, formData).subscribe(
      (attachment) => {
        console.log(attachment);

      },
      () => {
        console.log("error");
      }
    )

  }
}

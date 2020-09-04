import {Component, Input, OnInit} from '@angular/core';
import {CandidateAttachmentService} from '../../../services/candidate-attachment.service';
import {AttachmentType, CandidateAttachment} from '../../../model/candidate-attachment';
import {FormBuilder, FormGroup} from '@angular/forms';
import {environment} from '../../../../environments/environment';
import {CandidateService} from '../../../services/candidate.service';
import {forkJoin, Observable} from 'rxjs';
import {UserService} from '../../../services/user.service';
import {User} from '../../../model/user';

@Component({
  selector: 'app-candidate-attachments',
  templateUrl: './candidate-attachments.component.html',
  styleUrls: ['./candidate-attachments.component.scss']
})
export class CandidateAttachmentsComponent implements OnInit {

  @Input() preview: boolean = false;
  @Input() cv: boolean;

  error: any;
  _loading = {
    candidate: true,
    attachments: true,
    user: true
  };
  deleting: boolean;
  uploading: boolean;

  s3BucketUrl: string = environment.s3BucketUrl;
  form: FormGroup;

  attachments: CandidateAttachment[] = [];
  candidateNumber: string;
  user: User;

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService,
              private candidateAttachmentService: CandidateAttachmentService,
              private userService: UserService) { }

  ngOnInit() {
    this._loading.candidate = true;
    this._loading.user = true;

    this.candidateService.getCandidateNumber().subscribe(
      (response) => {
        this.candidateNumber = response.candidateNumber;
        this._loading.candidate = false;
      },
      (error) => {
        this.error = error;
        this._loading.candidate = false;
      });
    this.userService.getMyUser().subscribe(
      (response) => {
        this.user = response;
        this._loading.user = false;
      },
      (error) => {
        this.error = error;
        this._loading.user = false;
      }
    )
    this.refreshAttachments();

  }

  private refreshAttachments() {
    this._loading.attachments = true;
    this.candidateAttachmentService.listCandidateAttachments().subscribe(
      (response) => {
        if (!this.preview){
          this.attachments = response.filter(att => att.cv === this.cv);
        } else {
          this.attachments = response;
        }
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

  getAttachmentUrl(att: CandidateAttachment) {
    if (att.type === AttachmentType.file) {
      return this.s3BucketUrl + '/candidate/' + (att.migrated ? 'migrated' : this.candidateNumber) + '/' + att.location;
    }
    return att.location;
  }

  deleteAttachment(attachment: CandidateAttachment) {
    this.deleting = true;
    this.candidateAttachmentService.deleteAttachment(attachment.id).subscribe(
      (response) => {
        this.attachments = this.attachments.filter(att => att.name !== attachment.name);
        this.deleting = false;
      },
      (error) => {
        this.error = error;
        this.deleting = false;
      });
  }

  startServerUpload(files: File[]) {
    this.error = null;
    this.uploading = true;
    this.attachments = [];

    const uploads: Observable<CandidateAttachment>[] = [];
    for (const file of files) {
      const formData: FormData = new FormData();
      formData.append('file', file);

      uploads.push(this.candidateAttachmentService
        .uploadAttachment(this.cv, formData));
    }

    forkJoin(...uploads).subscribe(
      (results: CandidateAttachment[]) => {
        this.uploading = false;
        this.refreshAttachments();
      },
      error => {
        this.error = error;
        this.uploading = false;
      }
    );

  }
}

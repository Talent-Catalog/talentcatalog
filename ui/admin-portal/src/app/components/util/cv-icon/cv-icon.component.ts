import {Component, Input, OnInit} from '@angular/core';
import {
  AttachmentType,
  CandidateAttachment,
  SearchCandidateAttachmentsRequest
} from '../../../model/candidate-attachment';
import {environment} from '../../../../environments/environment';
import {CandidateAttachmentService} from '../../../services/candidate-attachment.service';
import {Candidate} from '../../../model/candidate';
import {saveBlob} from "../../../util/file";

@Component({
  selector: 'app-cv-icon',
  templateUrl: './cv-icon.component.html',
  styleUrls: ['./cv-icon.component.scss']
})
export class CvIconComponent implements OnInit {
  // Required Input
  @Input() candidate: Candidate;
  // Optional Input - if a candidate attachment is passed in, this will only open the single attachment.
  @Input() attachment: CandidateAttachment;

  cvs: CandidateAttachment[];
  s3BucketUrl = environment.s3BucketUrl;

  constructor(private candidateAttachmentService: CandidateAttachmentService) { }

  ngOnInit() {
    this.getAttachments();
  }

  getAttachments() {
    this.cvs = [];
    // If there is a single attachment passed down
    if (this.attachment) {
      this.cvs.push(this.attachment)
    } else {
      // Otherwise get all attachments
      const request: SearchCandidateAttachmentsRequest = {
        candidateId: this.candidate.id,
        cvOnly: true
      }
      this.candidateAttachmentService.search(request).subscribe(
        results => {
          this.cvs = results;
        },
        error => {
          console.log(error);
        })
      ;
    }
  }

  getAttachmentUrl(att: CandidateAttachment) {
    if (att.type === AttachmentType.file) {
      return this.s3BucketUrl + '/candidate/' + (att.migrated ? 'migrated' : this.candidate.candidateNumber) + '/' + att.location;
    }
    return att.location;
  }

  openCVs() {
    for (let i = 0; i < this.cvs.length; i++) {
      const cv = this.cvs[i];
      if (cv.type === AttachmentType.googlefile) {
        this.downloadCandidateAttachment(cv)
      } else {
        const newTab = window.open();
        const url = this.getAttachmentUrl(cv);
        newTab.location.href = url;
      }
    }
  }

  downloadCandidateAttachment(attachment: CandidateAttachment) {
    this.candidateAttachmentService.downloadAttachment(attachment.id).subscribe(
      (resp: Blob) => {
        saveBlob(resp, attachment.name);
      },
      (error) => {
        console.log(error);
      });
  }

}

import {Component, Input, OnInit} from '@angular/core';
import {AttachmentType, CandidateAttachment, SearchCandidateAttachmentsRequest} from '../../../model/candidate-attachment';
import {environment} from '../../../../environments/environment';
import {CandidateAttachmentService} from '../../../services/candidate-attachment.service';
import {Candidate} from '../../../model/candidate';

@Component({
  selector: 'app-cv-icon',
  templateUrl: './cv-icon.component.html',
  styleUrls: ['./cv-icon.component.scss']
})
export class CvIconComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() attachment: CandidateAttachment;

  cvs: CandidateAttachment[];
  loading: boolean;
  error;
  s3BucketUrl = environment.s3BucketUrl;

  constructor(private candidateAttachmentService: CandidateAttachmentService) { }

  ngOnInit() {
    this.getAttachments();
  }

  getAttachments() {
    this.cvs = [];
    this.loading = true;
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
          this.loading = false;
        },
        error => {
          this.error = error;
          this.loading = false;
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
      const newTab = window.open();
      const url = this.getAttachmentUrl(this.cvs[i]);
      newTab.location.href = url;
    }
  }

}

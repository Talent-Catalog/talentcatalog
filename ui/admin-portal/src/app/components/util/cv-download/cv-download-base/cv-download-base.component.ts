import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CandidateAttachmentService} from "../../../../services/candidate-attachment.service";
import {Candidate} from "../../../../model/candidate";
import {CandidateAttachment} from "../../../../model/candidate-attachment";
import {AuthorizationService} from "../../../../services/authorization.service";

@Component({
  selector: 'app-cv-download-base',
  templateUrl: './cv-download-base.component.html',
  styleUrls: ['./cv-download-base.component.scss']
})
export class CvDownloadBaseComponent implements OnInit {

  // Required
  @Input() candidate: Candidate;

  // Optional: if a candidate attachment is passed in, will only open the single attachment.
  @Input() attachment: CandidateAttachment;

  // Used to indicate loading status
  @Output() loadingStatus = new EventEmitter<boolean>();

  // Used to indicate error status
  @Output() errorStatus = new EventEmitter<string>();

  cvs: CandidateAttachment[];
  loading: boolean = false;
  error: string;

  constructor(
    private authService: AuthorizationService,
    private candidateAttachmentService: CandidateAttachmentService
  ) { }

  ngOnInit() {
    this.getAttachments();
  }

  getAttachments() {
    this.cvs = [];
    // If there is a single attachment passed down
    if (this.attachment) {
      this.cvs.push(this.attachment)
    } else {
      this.candidate.candidateAttachments.forEach(attachment => {
        if (attachment.cv) {
          this.cvs.push(attachment);
        }
      })
    }
  }

  canOpenOrDownloadCvs(): boolean {
    return this.authService.canViewCandidateCV() && this.cvs?.length > 0;
  }

  openOrDownloadCvs(event: any) {
    event.stopPropagation();
    this.loading = true;
    this.candidateAttachmentService.downloadAttachments(this.candidate, this.cvs).subscribe(
      () => this.loading = false,
      (error: string) => {
        this.loading = false; this.errorStatus.emit(error);
        this.error = error;
      }
    );
  }

}

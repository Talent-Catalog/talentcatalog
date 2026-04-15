import {Component, Input, OnInit} from '@angular/core';
import {IssueReportRequest, ServiceAssignment} from "../../../../../../../model/services";
import {Candidate} from "../../../../../../../model/candidate";
import {LinkedinService} from "../../../../../../../services/linkedin.service";

@Component({
  selector: 'app-linkedin-redeemed',
  templateUrl: './linkedin-redeemed.component.html',
  styleUrl: './linkedin-redeemed.component.scss'
})
export class LinkedinRedeemedComponent implements OnInit {
  @Input() assignment: ServiceAssignment;
  @Input() candidate: Candidate;
  isOnIssueReportList = false;
  showIssueForm = false;
  issueComment = '';
  readonly MAX_COMMENT_LENGTH = 500;
  error: any;
  loading = false;

  constructor(private linkedinService: LinkedinService) { }

  ngOnInit() {
    this.checkIsOnIssueReportList();
  }

  toggleIssueForm() {
    this.showIssueForm = !this.showIssueForm;
  }

  /** Puts candidate on #LinkedInIssueReport List for admin action */
  reportIssue() {
    this.loading = true;
    this.error = null;

    const request: IssueReportRequest = {
      assignment: this.assignment,
      issueComment: this.issueComment,
    };

    this.linkedinService.addCandidateToIssueReportList(request).subscribe({
      next: () => {
        this.checkIsOnIssueReportList();
        this.loading = false;
      },
      error: (error) => {
        this.error = error;
        this.loading = false;
      }
    });
  }

  private checkIsOnIssueReportList() {
    this.loading = true;
    this.error = null;

    this.linkedinService.isOnIssueReportList(this.candidate.id).subscribe({
      next: (result) => {
        this.isOnIssueReportList = result;
        this.loading = false;
      },
      error: (error) => {
        this.error = error;
        this.loading = false;
      }
    });
  }

}

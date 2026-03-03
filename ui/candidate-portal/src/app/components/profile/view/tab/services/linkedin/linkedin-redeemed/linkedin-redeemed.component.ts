import {Component, Input, OnInit} from '@angular/core';
import {ServiceAssignment} from "../../../../../../../model/services";
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
  error: any;
  loading = false;

  constructor(private linkedinService: LinkedinService) { }

  ngOnInit() {
    this.checkIsOnIssueReportList();
  }

  reportIssue() {
    this.linkedinService.addCandidateToIssueReportList(this.assignment).subscribe({
      next: () => {
        this.checkIsOnIssueReportList();
      },
      error: (error) => this.error = error
    });
  }

  private checkIsOnIssueReportList() {
    this.linkedinService.isOnIssueReportList(this.candidate.id).subscribe({
      next: (result) => this.isOnIssueReportList = result,
      error: (error) => this.error = error
    });
  }

}

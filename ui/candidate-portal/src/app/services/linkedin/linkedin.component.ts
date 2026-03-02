import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Candidate} from "../../model/candidate";
import {LinkedinService} from "../linkedin.service";
import {CandidateService} from "../candidate.service";
import {of} from "rxjs";
import {switchMap} from "rxjs/operators";
import {
  AssignmentStatus,
  ResourceStatus,
  ServiceAssignment,
  UpdateServiceResourceStatusRequest
} from "../../model/services";

@Component({
  selector: 'app-linkedin',
  templateUrl: './linkedin.component.html',
  styleUrl: './linkedin.component.scss'
})
export class LinkedinComponent {
  @Input() candidate: Candidate;
  @Output() backButtonClicked = new EventEmitter<void>();
  assignment?: ServiceAssignment;
  linkedInLinkInput: string;
  verified = false;
  loading: boolean;
  error: any;

  constructor(
    private linkedinService: LinkedinService,
    private candidateService: CandidateService,
  ) { }

  ngOnInit() {
    this.linkedInLinkInput = this.candidate.linkedInLink ?? '';
    this.linkedinService.findRedeemedOrAssignedCoupon(this.candidate.id)
      .subscribe(assignment => this.assignment = assignment);
  }

  /**
   * Verifies the candidate's LinkedIn URL and assigns a coupon.
   * If the URL has changed, updates the candidate's profile first.
   */
  verify() {
    const update$ = this.linkedInLinkInput !== this.candidate.linkedInLink
      ? this.candidateService.updateCandidateOtherInfo({ linkedInLink: this.linkedInLinkInput })
      : of(null);

    update$.pipe(
      switchMap(() => this.linkedinService.assign(this.candidate.id))
    ).subscribe({
      next: (assignment) => {
        this.verified = true;
        this.assignment = assignment;
      },
      error: (error) => this.error = error
    });
  }

  redeem() {
    if (this.assignment) {
      const request: UpdateServiceResourceStatusRequest = {
        resourceCode: this.assignment.resource.resourceCode,
        status: ResourceStatus.REDEEMED
      }

      this.linkedinService.updateCouponStatus(request).subscribe({
        next: () => {
          // Reassigning the whole object here triggers change detection.
          this.assignment = { ...this.assignment, status: AssignmentStatus.REDEEMED };
        },
        error: (error) => this.error = error
      })
    }
  }

  get canRedeem(): boolean {
    return this.assignment && this.assignment.status !== AssignmentStatus.REDEEMED;
  }

  onBackButtonClicked() {
    this.backButtonClicked.emit();
  }

}

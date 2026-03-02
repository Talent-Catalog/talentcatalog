import {Component, Input} from '@angular/core';
import {Candidate} from "../../model/candidate";
import {LinkedinService} from "../linkedin.service";
import {CandidateService} from "../candidate.service";
import {of} from "rxjs";
import {switchMap} from "rxjs/operators";
import {
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
  assignment?: ServiceAssignment;
  linkedInLink: string;
  redeemed = false;
  verified = false;
  loading: boolean;
  error: any;

  constructor(
    private linkedinService: LinkedinService,
    private candidateService: CandidateService,
  ) { }

  ngOnInit() {
    this.linkedInLink = this.candidate.linkedInLink ?? '';
    this.linkedinService.hasRedeemed(this.candidate.id)
    .subscribe(hasRedeemed => this.redeemed = hasRedeemed);
  }

  /**
   * Verifies the candidate's LinkedIn URL and assigns a coupon.
   * If the URL has changed, updates the candidate's profile first.
   */
  verify() {
    const update$ = this.linkedInLink !== this.candidate.linkedInLink
      ? this.candidateService.updateCandidateOtherInfo({ linkedInLink: this.linkedInLink })
      : of(null);

    update$.pipe(
      switchMap(() => this.linkedinService.assign(this.candidate.id))
    ).subscribe({
      next: () => {
        this.candidate.linkedInLink = this.linkedInLink;
        this.verified = true;
        // TODO set coupon info ready for redeem
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
          this.redeemed = true;
        },
        error: (error) => this.error = error
      })
    }
  }

  // TODO what if candidate already has assigned coupon but not yet redeemed?

}

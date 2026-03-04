import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Candidate} from "../../../../../../model/candidate";
import {LinkedinService} from "../../../../../../services/linkedin.service";
import {CandidateService} from "../../../../../../services/candidate.service";
import {of} from "rxjs";
import {switchMap} from "rxjs/operators";
import {
  ResourceStatus,
  ServiceAssignment,
  UpdateServiceResourceStatusRequest
} from "../../../../../../model/services";

@Component({
  selector: 'app-linkedin',
  templateUrl: './linkedin.component.html',
  styleUrl: './linkedin.component.scss'
})
export class LinkedinComponent implements OnInit {
  protected readonly ResourceStatus = ResourceStatus;
  @Input() candidate!: Candidate;
  @Output() backButtonClicked = new EventEmitter<void>();
  assignment?: ServiceAssignment;
  isOnAssignmentFailureList = false;
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
    this.loadAssignment();
    this.checkIsOnAssignmentFailureList();
  }

  /**
   * Verifies the candidate's LinkedIn URL and assigns a coupon.
   * If the URL has changed, updates the candidate's profile first.
   */
  verify() {
    this.loading = true;
    this.error = null;

    const update$ = this.linkedInLinkInput !== this.candidate.linkedInLink
      ? this.candidateService.updateCandidateOtherInfo({ linkedInLink: this.linkedInLinkInput })
      : of(null);

    update$.pipe(
      switchMap(() => this.linkedinService.assign(this.candidate.id))
    ).subscribe({
      next: (assignment) => {
        this.verified = true;
        this.assignment = assignment;
        this.checkIsOnAssignmentFailureList();
        this.loading = false;
      },
      error: (error) => {
        this.error = error;
        this.loading = false;
      }
    });
  }

  /** Updates resource status to REDEEMED */
  redeem() {
    if (this.assignment) {
      this.loading = true;
      this.error = null;

      const request: UpdateServiceResourceStatusRequest = {
        resourceCode: this.assignment.resource.resourceCode,
        status: ResourceStatus.REDEEMED
      }

      this.linkedinService.updateCouponStatus(request).subscribe({
        next: () => {
          // Reassigning the whole object here triggers change detection.
          this.assignment = {
            ...this.assignment,
            resource: {...this.assignment.resource, status: ResourceStatus.REDEEMED}
          };
          window.open(this.assignment.resource.resourceCode, '_blank');
          this.loading = false;
        },
        error: (error) => {
          this.error = error;
          this.loading = false;
        }
      })
    }
  }

  get canRedeem(): boolean {
    return this.assignment && this.assignment.resource.status !== ResourceStatus.REDEEMED;
  }

  onBackButtonClicked() {
    this.backButtonClicked.emit();
  }

  get isValidLinkedInUrl(): boolean {
    const linkedInRegex = /^http(s)?:\/\/([\w]+\.)?linkedin\.com\/in\/[A-z0-9_-]+\/?/;
    return linkedInRegex.test(this.linkedInLinkInput);
  }

  private loadAssignment(): void {
    this.loading = true;
    this.error = null;

    this.linkedinService.findAssignmentWithReservedOrRedeemedResource(this.candidate.id)
    .subscribe({
      next: (assignment) => {
        this.assignment = assignment;
        this.loading = false;
      },
      error: (error) => {
        this.error = error;
        this.loading = false;
      }
    });
  }

  private checkIsOnAssignmentFailureList() {
    this.loading = true;
    this.error = null;

    this.linkedinService.isOnAssignmentFailureList(this.candidate.id).subscribe({
      next: (result) => {
        this.isOnAssignmentFailureList = result;
        this.loading = false;
      },
      error: (error) => {
        this.error = error;
        this.loading = false;
      }
    });
  }

}

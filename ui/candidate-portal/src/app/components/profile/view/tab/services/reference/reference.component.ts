import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {forkJoin} from "rxjs";
import {Candidate} from "../../../../../../model/candidate";
import {
  ResourceStatus,
  ServiceAssignment,
  ServiceProviderTermsInfo,
  UpdateServiceResourceStatusRequest
} from "../../../../../../model/services";
import {CasiPortalService} from "../../../../../../services/casi-portal.service";


/**
 * Component for managing reference vouchers in the candidate portal.
 * Allows candidates to view their current voucher assignment, check eligibility,
 * assign a new voucher if eligible, and redeem the voucher. Handles loading states
 * and error messages for all operations.
 *
 * @author sadatmalik
 */
@Component({
  selector: 'app-reference',
  templateUrl: './reference.component.html',
  styleUrl: './reference.component.scss'
})
export class ReferenceComponent implements OnInit {
  protected readonly ResourceStatus = ResourceStatus;
  @Input() candidate!: Candidate;
  @Output() backButtonClicked = new EventEmitter<void>();
  assignment?: ServiceAssignment;
  eligible = false;
  needsAgreement = false;
  termsInfo: ServiceProviderTermsInfo | null = null;
  termsRead = false;
  loading: boolean;
  error: any;

  private readonly provider = 'REFERENCE';
  private readonly serviceCode = 'VOUCHER';

  constructor(private portalService: CasiPortalService) { }

  ngOnInit() {
    this.checkEligibilityAndLoad();
  }

  assign() {
    this.loading = true;
    this.error = null;
    this.portalService.assign(this.provider, this.serviceCode).subscribe({
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

  redeem() {
    if (!this.assignment) {
      return;
    }
    this.loading = true;
    this.error = null;
    const request: UpdateServiceResourceStatusRequest = {
      resourceCode: this.assignment.resource.resourceCode,
      status: ResourceStatus.REDEEMED
    };

    this.portalService.updateResourceStatus(this.provider, this.serviceCode, request).subscribe({
      next: () => {
        this.assignment = {
          ...this.assignment,
          resource: { ...this.assignment.resource, status: ResourceStatus.REDEEMED }
        };
        this.loading = false;
      },
      error: (error) => {
        this.error = error;
        this.loading = false;
      }
    });
  }

  onBackButtonClicked() {
    this.backButtonClicked.emit();
  }

  setTermsRead() {
    this.termsRead = true;
  }

  acceptTerms() {
    this.loading = true;
    this.error = null;
    this.portalService.acceptProviderTerms(this.provider, this.serviceCode).subscribe({
      next: () => {
        this.termsRead = false;
        this.checkEligibilityAndLoad();
      },
      error: (error) => {
        this.error = error;
        this.loading = false;
      }
    });
  }

  get canRedeem(): boolean {
    return !!this.assignment && this.assignment.resource.status !== ResourceStatus.REDEEMED;
  }

  private checkEligibilityAndLoad() {
    this.loading = true;
    this.error = null;

    this.portalService.checkEligibility(this.provider, this.serviceCode).subscribe({
      next: eligible => {
        this.eligible = eligible;
        if (!eligible) {
          this.loading = false;
          return;
        }

        forkJoin({
          assignment: this.portalService.getAssignment(this.provider, this.serviceCode),
          needsAgreement: this.portalService.checkNeedsAgreement(this.provider, this.serviceCode),
          termsInfo: this.portalService.getProviderTerms(this.provider, this.serviceCode)
        }).subscribe({
          next: result => {
            this.assignment = result.assignment;
            this.needsAgreement = result.needsAgreement;
            this.termsInfo = result.termsInfo;
            this.loading = false;
          },
          error: error => {
            this.error = error;
            this.loading = false;
          }
        });
      },
      error: (error) => {
        this.error = error;
        this.loading = false;
      }
    });
  }
}

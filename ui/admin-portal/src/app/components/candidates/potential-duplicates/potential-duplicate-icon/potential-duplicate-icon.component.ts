import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Candidate} from "../../../../model/candidate";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DuplicatesDetailComponent} from "../duplicates-detail/duplicates-detail.component";
import {CandidateService} from "../../../../services/candidate.service";
import {AuthorizationService} from "../../../../services/authorization.service";

/**
 * Provides an icon indicating candidate may be a duplicate profile, for displaying on candidate rows.
 * Opens {@link DuplicatesDetailComponent} modal for reviewing results.
 */
@Component({
  selector: 'app-potential-duplicate-icon',
  templateUrl: './potential-duplicate-icon.component.html',
  styleUrls: ['./potential-duplicate-icon.component.scss']
})
export class PotentialDuplicateIconComponent {
  @Input('candidate') candidate: Candidate;
  @Output() refresh: EventEmitter<void> = new EventEmitter();
  error = null;
  loading = null;

  constructor(
    protected modalService: NgbModal,
    private candidateService: CandidateService,
    private authorizationService: AuthorizationService,
  ) { }

  public openDuplicateDetailModal(): void {
    // Modal
    const duplicateDetailModal = this.modalService.open(DuplicatesDetailComponent, {
      centered: true,
      backdrop: 'static'
    });

    duplicateDetailModal.componentInstance.selectedCandidate = this.candidate;

    // When the modal is closed or dismissed, the server will update the candidate's potential
    // duplicate property if any change (i.e. it is no longer a duplicate), and this component emits
    // an event to refresh the parent view in case the icon no longer needs to be displayed.
    duplicateDetailModal.result.then(() => {
      this.updateCandidate();
    }).catch(() => {
      this.updateCandidate();
    });
  }

  // Essentially a failsafe if the user has not used the 'Refresh' button in the modal — helps to
  // keep data ahead of the daily background duplicate check.
  private updateCandidate(): void {
    this.loading = true;
    this.candidateService.fetchPotentialDuplicates(this.candidate.id).subscribe(
      result => {
        if (result.length === 0) this.refresh.emit();
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  public canViewCandidateName() {
    return this.authorizationService.canViewCandidateName();
  }

}

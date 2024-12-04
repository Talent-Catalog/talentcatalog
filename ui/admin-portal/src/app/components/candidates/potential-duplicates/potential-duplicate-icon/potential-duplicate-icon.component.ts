import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Candidate} from "../../../../model/candidate";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DuplicatesDetailComponent} from "../duplicates-detail/duplicates-detail.component";
import {CandidateService} from "../../../../services/candidate.service";

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
    private candidateService: CandidateService
  ) { }

  public openDuplicateDetailModal(): void {
    // Modal
    const duplicateDetailModal = this.modalService.open(DuplicatesDetailComponent, {
      centered: true,
      backdrop: 'static'
    });

    duplicateDetailModal.componentInstance.selectedCandidate = this.candidate;

    // When the modal is closed or dismissed, refresh the parent view with updated data.
    duplicateDetailModal.result.then(() => {
      this.updateCandidate();
    }).catch(() => {
      this.updateCandidate();
    });
  }

  private updateCandidate(): void {
    this.loading = true;
    this.candidateService.fetchPotentialDuplicates(this.candidate.id).subscribe(
      result => {
        this.refresh.emit();
      },
      error => {
        this.error = error;
        this.loading = false;
      }
    );
  }

}

import {Component, HostListener} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../services/candidate.service";
import {Candidate} from "../../../../model/candidate";

@Component({
  selector: 'app-duplicates-detail',
  templateUrl: './duplicates-detail.component.html',
  styleUrls: ['./duplicates-detail.component.scss']
})
export class DuplicatesDetailComponent {
  error = null;
  loading = null;
  selectedCandidate: Candidate;
  potentialDuplicates: Candidate[];

  constructor(
    private activeModal: NgbActiveModal,
    private candidateService: CandidateService
  ) { }

  ngOnInit(): void {
    this.fetchPotentialDuplicates(this.selectedCandidate.id);
  }

  private fetchPotentialDuplicates(candidateId: number) {
    this.loading = true;
    this.candidateService.fetchPotentialDuplicates(candidateId).subscribe(
      result => {
        this.potentialDuplicates = result;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  public refresh(): void {
    this.fetchPotentialDuplicates(this.selectedCandidate.id);
  }

  // Emit event when modal is closed
  closeModal() {
    this.activeModal.close()
  }

}

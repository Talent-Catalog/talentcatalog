import { Component } from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../services/candidate.service";

@Component({
  selector: 'app-duplicates-detail',
  templateUrl: './duplicates-detail.component.html',
  styleUrls: ['./duplicates-detail.component.scss']
})
export class DuplicatesDetailComponent {
  error = null;
  loading = null;
  candidateId: number;

  constructor(
    private activeModal: NgbActiveModal,
    private candidateService: CandidateService
  ) { }

  ngOnInit(): void {
    this.fetchPotentialDuplicates(this.candidateId);
  }

  private fetchPotentialDuplicates(candidateId: number) {
    this.loading = true;
    this.candidateService.fetchPotentialDuplicates(candidateId).subscribe(
      result => {
        // display results
      },
      error => {
        this.error = error;
      }
    );
  }

  public closeModal(): void {
    this.activeModal.close();
  }

  //TODO:
  public resolveButtonClicked() {

  }
}

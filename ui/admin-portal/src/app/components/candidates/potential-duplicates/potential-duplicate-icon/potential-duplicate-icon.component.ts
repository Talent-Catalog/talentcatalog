import {Component, Input} from '@angular/core';
import {Candidate} from "../../../../model/candidate";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DuplicatesDetailComponent} from "../duplicates-detail/duplicates-detail.component";

@Component({
  selector: 'app-potential-duplicate-icon',
  templateUrl: './potential-duplicate-icon.component.html',
  styleUrls: ['./potential-duplicate-icon.component.scss']
})
export class PotentialDuplicateIconComponent {
  @Input('candidate') candidate: Candidate;

  constructor(protected modalService: NgbModal) { }

  public openDuplicateDetailModal(): void {
    // Modal
    const duplicateDetailModal = this.modalService.open(DuplicatesDetailComponent, {
      centered: true,
      backdrop: 'static'
    });

    duplicateDetailModal.componentInstance.candidateId = this.candidate.id;

    duplicateDetailModal.result
    .then((result) => {
    })
    .catch(() => { /* Isn't possible */ });
  }
}

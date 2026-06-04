import {Component, Input} from '@angular/core';
import {Candidate} from "../../../../model/candidate";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../services/candidate.service";
import {EditCandidateAspirationsComponent} from "./edit/edit-candidate-aspirations.component";

@Component({
  selector: 'app-view-candidate-aspirations',
  templateUrl: './view-candidate-aspirations.component.html',
  styleUrl: './view-candidate-aspirations.component.scss'
})
export class ViewCandidateAspirationsComponent {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  constructor(private modalService: NgbModal,
              private candidateService: CandidateService) { }

  editAspirations() {
    const editAspirationsModal = this.modalService.open(EditCandidateAspirationsComponent, {
      centered: true,
      backdrop: 'static'
    });

    editAspirationsModal.componentInstance.candidateId = this.candidate.id;

    editAspirationsModal.result
    .then((candidate) => this.candidateService.updateCandidate())
    .catch(() => { /* Isn't possible */ });

  }

}

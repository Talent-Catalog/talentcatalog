import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../model/candidate";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EditCandidateAdditionalInfoComponent} from "./edit/edit-candidate-additional-info.component";

@Component({
  selector: 'app-view-candidate-additional-info',
  templateUrl: './view-candidate-additional-info.component.html',
  styleUrls: ['./view-candidate-additional-info.component.scss']
})
export class ViewCandidateAdditionalInfoComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  constructor(private modalService: NgbModal) { }

  ngOnInit() {
  }

  editAdditionalInfo() {
    const editAdditionalInfoModal = this.modalService.open(EditCandidateAdditionalInfoComponent, {
      centered: true,
      backdrop: 'static'
    });

    editAdditionalInfoModal.componentInstance.candidateId = this.candidate.id;

    editAdditionalInfoModal.result
      .then((candidate) => this.candidate = candidate)
      .catch(() => { /* Isn't possible */ });

  }

}

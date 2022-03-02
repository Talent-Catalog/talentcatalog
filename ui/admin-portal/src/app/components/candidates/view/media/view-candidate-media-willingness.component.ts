import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../model/candidate";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EditCandidateMediaWillingnessComponent} from "./edit/edit-candidate-media-willingness.component";

@Component({
  selector: 'app-view-candidate-media-willingness',
  templateUrl: './view-candidate-media-willingness.component.html',
  styleUrls: ['./view-candidate-media-willingness.component.scss']
})
export class ViewCandidateMediaWillingnessComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  constructor(private modalService: NgbModal) { }

  ngOnInit() {
  }

  editMediaWillingness() {
    const editMediaWillingnessModal = this.modalService.open(EditCandidateMediaWillingnessComponent, {
      centered: true,
      backdrop: 'static'
    });

    editMediaWillingnessModal.componentInstance.candidateId = this.candidate.id;

    editMediaWillingnessModal.result
      .then((candidate) => this.candidate = candidate)
      .catch(() => { /* Isn't possible */ });

  }

}

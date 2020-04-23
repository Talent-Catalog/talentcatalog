import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../model/candidate";
import {EditCandidateSurveyComponent} from "./edit/edit-candidate-survey.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-view-candidate-survey',
  templateUrl: './view-candidate-survey.component.html',
  styleUrls: ['./view-candidate-survey.component.scss']
})
export class ViewCandidateSurveyComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  constructor(private modalService: NgbModal) { }

  ngOnInit() {
  }

  editSurvey() {
    const editSurveyModal = this.modalService.open(EditCandidateSurveyComponent, {
      centered: true,
      backdrop: 'static'
    });

    editSurveyModal.componentInstance.candidateId = this.candidate.id;

    editSurveyModal.result
      .then((candidate) => this.candidate = candidate)
      .catch(() => { /* Isn't possible */ });

  }

}

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from "../../../../model/candidate";
import {CandidateEducation} from "../../../../model/candidate-education";
import {CandidateEducationService} from "../../../../services/candidate-education.service";
import {EditCandidateEducationComponent} from "./edit/edit-candidate-education.component";
import {CreateCandidateEducationComponent} from "./create/create-candidate-education.component";

@Component({
  selector: 'app-view-candidate-education',
  templateUrl: './view-candidate-education.component.html',
  styleUrls: ['./view-candidate-education.component.scss']
})
export class ViewCandidateEducationComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  candidateEducations: CandidateEducation[];
  candidateEducation: CandidateEducation;
  loading: boolean;
  error;

  constructor(private candidateEducationService: CandidateEducationService,
              private modalService: NgbModal ) {
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    this.editable = true;
    console.log(changes);
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.loading = true;
      this.candidateEducationService.list(this.candidate.id).subscribe(
        candidateEducations => {
          this.candidateEducations = candidateEducations;
          this.loading = false;
        },
        error => {
          this.error = error;
          this.loading = false;
        })
      ;
    }
  }

  editCandidateEducation(candidateEducation: CandidateEducation) {
    const editCandidateEducationModal = this.modalService.open(EditCandidateEducationComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateEducationModal.componentInstance.candidateEducation = candidateEducation;

    editCandidateEducationModal.result
      .then((candidateEducation) => this.candidateEducation = candidateEducation)
      .catch(() => { /* Isn't possible */ });

  }

  createCandidateEducation() {
    const createCandidateEducationModal = this.modalService.open(CreateCandidateEducationComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateEducationModal.componentInstance.candidateId = this.candidate.id;

    createCandidateEducationModal.result
      .then((candidateEducation) => this.candidateEducation = candidateEducation)
      .catch(() => { /* Isn't possible */ });

  }


}

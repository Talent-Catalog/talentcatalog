import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from "../../../../model/candidate";
import {CandidateCertification} from "../../../../model/candidate-certification";
import {CandidateCertificationService} from "../../../../services/candidate-certification.service";
import {EditCandidateCertificationComponent} from "./edit/edit-candidate-certification.component";
import {CreateCandidateCertificationComponent} from "./create/create-candidate-certification.component";

@Component({
  selector: 'app-view-candidate-certification',
  templateUrl: './view-candidate-certification.component.html',
  styleUrls: ['./view-candidate-certification.component.scss']
})
export class ViewCandidateCertificationComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  candidateCertifications: CandidateCertification[];
  candidateCertification: CandidateCertification;
  loading: boolean;
  error;

  constructor(private candidateCertificationService: CandidateCertificationService,
              private modalService: NgbModal) {
  }

  ngOnInit() {

  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.loading = true;
      this.candidateCertificationService.list(this.candidate.id).subscribe(
        candidateCertifications => {
          this.candidateCertifications = candidateCertifications;
          this.loading = false;
        },
        error => {
          this.error = error;
          this.loading = false;
        })
      ;
    }
  }

  editCandidateCertification(candidateCertification: CandidateCertification) {
    const editCandidateCertificationModal = this.modalService.open(EditCandidateCertificationComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateCertificationModal.componentInstance.candidateCertification = candidateCertification;

    editCandidateCertificationModal.result
      .then((candidateCertification) => this.candidateCertification = candidateCertification)
      .catch(() => { /* Isn't possible */ });

  }

  createCandidateCertification() {
    const createCandidateCertificationModal = this.modalService.open(CreateCandidateCertificationComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateCertificationModal.componentInstance.candidateId = this.candidate.id;

    createCandidateCertificationModal.result
      .then((candidateCertification) => this.candidateCertification = candidateCertification)
      .catch(() => { /* Isn't possible */ });

  }

}

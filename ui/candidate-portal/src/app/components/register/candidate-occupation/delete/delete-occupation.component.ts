import { Component, OnInit } from '@angular/core';
import {CandidateOccupation} from "../../../../model/candidate-occupation";
import {CandidateOccupationService} from "../../../../services/candidate-occupation.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-delete-occupation',
  templateUrl: './delete-occupation.component.html',
  styleUrls: ['./delete-occupation.component.scss']
})
export class DeleteOccupationComponent implements OnInit {

  candidateOccupationId: number;
  deleting: boolean;

  constructor(private activeModal: NgbActiveModal,
              private candidateOccupationService: CandidateOccupationService) { }

  ngOnInit() {
  }

  cancel() {
    this.activeModal.dismiss(false);
  }

  confirm() {
    this.deleting = true;
    this.activeModal.close(true);
    // this.candidateOccupationService.deleteCandidateOccupation(this.candidateOccupationId).subscribe(result => {
    //   this.deleting = false;
    //   this.activeModal.close();
    // });
  }

}

import { Component, OnInit } from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-delete-candidate-occupation',
  templateUrl: './delete-candidate-occupation.component.html',
  styleUrls: ['./delete-candidate-occupation.component.scss']
})
export class DeleteCandidateOccupationComponent implements OnInit {

  deleting: boolean;

  constructor(private activeModal: NgbActiveModal) { }

  ngOnInit() {
  }

  cancel() {
    this.activeModal.dismiss(false);
  }

  confirm() {
    this.deleting = true;
    this.activeModal.close(true);
  }

}

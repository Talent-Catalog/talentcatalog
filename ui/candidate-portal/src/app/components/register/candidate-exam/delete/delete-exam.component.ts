import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-delete',
  templateUrl: './delete-exam.component.html',
  styleUrls: ['./delete-exam.component.scss']
})
export class DeleteExamComponent implements OnInit {

  candidateExamId: number;
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

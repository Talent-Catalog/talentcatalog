import { Component, Injectable, OnInit } from '@angular/core';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-work-in-progress',
  templateUrl: './confirmation.component.html',
  styleUrls: ['./confirmation.component.scss']
})
export class ConfirmationComponent implements OnInit {

  title: string;
  message: string;

  constructor(private activeModal: NgbActiveModal) { }

  ngOnInit() {
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  close() {
    this.activeModal.close(true);
  }




}

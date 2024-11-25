import {Component, OnInit} from '@angular/core';
import {TaskAssignment} from "../../../../../model/task-assignment";
import {UntypedFormGroup} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-view-response',
  templateUrl: './view-response.component.html',
  styleUrls: ['./view-response.component.scss']
})
export class ViewResponseComponent implements OnInit {

  taskAssignment: TaskAssignment;
  form: UntypedFormGroup;
  dueDate: string;
  date: Date;
  loading;
  saving;
  error;

  constructor(private activeModal: NgbActiveModal) { }

  ngOnInit(): void {
  }

  close() {
    this.activeModal.close();
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}

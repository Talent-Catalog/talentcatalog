import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../../model/job";

@Component({
  selector: 'app-view-job-contact',
  templateUrl: './view-job-contact.component.html',
  styleUrls: ['./view-job-contact.component.scss']
})
export class ViewJobContactComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;

  constructor() { }

  ngOnInit(): void {
  }

  editContactDetails() {
    //todo
  }
}

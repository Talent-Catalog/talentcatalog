import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../../model/job";

@Component({
  selector: 'app-view-job-source-contacts',
  templateUrl: './view-job-source-contacts.component.html',
  styleUrls: ['./view-job-source-contacts.component.scss']
})
export class ViewJobSourceContactsComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;

  constructor() { }

  ngOnInit(): void {
  }

}

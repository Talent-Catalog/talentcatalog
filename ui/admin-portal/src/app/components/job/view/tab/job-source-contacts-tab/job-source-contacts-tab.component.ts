import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../../model/job";

@Component({
  selector: 'app-job-source-contacts-tab',
  templateUrl: './job-source-contacts-tab.component.html',
  styleUrls: ['./job-source-contacts-tab.component.scss']
})
export class JobSourceContactsTabComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;

  constructor() { }

  ngOnInit(): void {
  }

}

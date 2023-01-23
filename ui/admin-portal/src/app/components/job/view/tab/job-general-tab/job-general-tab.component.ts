import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../../model/job";

@Component({
  selector: 'app-job-general-tab',
  templateUrl: './job-general-tab.component.html',
  styleUrls: ['./job-general-tab.component.scss']
})
export class JobGeneralTabComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;

  constructor() { }

  ngOnInit(): void {
  }

}

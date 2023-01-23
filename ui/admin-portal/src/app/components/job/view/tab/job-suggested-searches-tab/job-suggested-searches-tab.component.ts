import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../../model/job";

@Component({
  selector: 'app-job-suggested-searches-tab',
  templateUrl: './job-suggested-searches-tab.component.html',
  styleUrls: ['./job-suggested-searches-tab.component.scss']
})
export class JobSuggestedSearchesTabComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;

  constructor() { }

  ngOnInit(): void {
  }

}

import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../../model/job";

@Component({
  selector: 'app-view-job-submission-list',
  templateUrl: './view-job-submission-list.component.html',
  styleUrls: ['./view-job-submission-list.component.scss']
})
export class ViewJobSubmissionListComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;

  constructor() { }

  ngOnInit(): void {
  }

}

import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../../model/job";

@Component({
  selector: 'app-view-job-suggested-list',
  templateUrl: './view-job-suggested-list.component.html',
  styleUrls: ['./view-job-suggested-list.component.scss']
})
export class ViewJobSuggestedListComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;

  constructor() { }

  ngOnInit(): void {
  }

}

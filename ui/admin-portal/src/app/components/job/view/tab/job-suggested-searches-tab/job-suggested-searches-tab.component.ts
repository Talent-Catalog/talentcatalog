import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Job} from "../../../../../model/job";

@Component({
  selector: 'app-job-suggested-searches-tab',
  templateUrl: './job-suggested-searches-tab.component.html',
  styleUrls: ['./job-suggested-searches-tab.component.scss']
})
export class JobSuggestedSearchesTabComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;
  @Output() jobUpdated = new EventEmitter<Job>();

  constructor() { }

  ngOnInit(): void {
  }

  onJobUpdated(job: Job) {
    this.jobUpdated.emit(job)
  }
}

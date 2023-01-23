import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Job} from "../../../../../model/job";

@Component({
  selector: 'app-job-upload-tab',
  templateUrl: './job-upload-tab.component.html',
  styleUrls: ['./job-upload-tab.component.scss']
})
export class JobUploadTabComponent implements OnInit {
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

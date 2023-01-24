import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Job} from "../../../../../model/job";
import {JobPrepItem} from "../../../../../model/job-prep-item";

@Component({
  selector: 'app-job-upload-tab',
  templateUrl: './job-upload-tab.component.html',
  styleUrls: ['./job-upload-tab.component.scss']
})
export class JobUploadTabComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;
  @Input() highlightItem: JobPrepItem;
  @Output() jobUpdated = new EventEmitter<Job>();

  constructor() { }

  ngOnInit(): void {
  }

  onJobUpdated(job: Job) {
    this.jobUpdated.emit(job)
  }
}

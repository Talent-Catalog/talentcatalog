import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../../model/job";

@Component({
  selector: 'app-job-upload-tab',
  templateUrl: './job-upload-tab.component.html',
  styleUrls: ['./job-upload-tab.component.scss']
})
export class JobUploadTabComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;

  constructor() { }

  ngOnInit(): void {
  }

}

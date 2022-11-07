import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../../model/job";

@Component({
  selector: 'app-view-job-uploads',
  templateUrl: './view-job-uploads.component.html',
  styleUrls: ['./view-job-uploads.component.scss']
})
export class ViewJobUploadsComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;

  error: any;
  saving: boolean;

  constructor() { }

  ngOnInit(): void {
  }

  editJd() {
    //todo
  }

  editJoi() {
    //todo
  }
}

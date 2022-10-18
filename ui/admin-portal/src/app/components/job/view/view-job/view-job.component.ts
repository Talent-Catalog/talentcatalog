import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../model/job";

@Component({
  selector: 'app-view-job',
  templateUrl: './view-job.component.html',
  styleUrls: ['./view-job.component.scss']
})
export class ViewJobComponent implements OnInit {
  @Input() job: Job;

  constructor() { }

  ngOnInit(): void {
  }
}

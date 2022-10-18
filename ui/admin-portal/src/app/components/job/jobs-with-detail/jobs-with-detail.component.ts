import {Component, OnInit} from '@angular/core';
import {Job} from "../../../model/job";

@Component({
  selector: 'app-jobs-with-detail',
  templateUrl: './jobs-with-detail.component.html',
  styleUrls: ['./jobs-with-detail.component.scss']
})
export class JobsWithDetailComponent implements OnInit {
  selectedJob: Job;

  constructor() { }

  ngOnInit(): void {
  }

  onJobSelected(job: Job) {
    this.selectedJob = job;
  }
}

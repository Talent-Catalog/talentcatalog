import {Component, OnInit} from '@angular/core';
import {Job} from "../../../model/job";
import {MainSidePanelBase} from "../../util/split/MainSidePanelBase";


//todo This could be base component for any split screen

@Component({
  selector: 'app-jobs-with-detail',
  templateUrl: './jobs-with-detail.component.html',
  styleUrls: ['./jobs-with-detail.component.scss']
})
export class JobsWithDetailComponent extends MainSidePanelBase implements OnInit {
  selectedJob: Job;

  constructor() {
    super(4, 6);
  }

  ngOnInit(): void {

  }

  onJobSelected(job: Job) {
    this.selectedJob = job;
  }

}

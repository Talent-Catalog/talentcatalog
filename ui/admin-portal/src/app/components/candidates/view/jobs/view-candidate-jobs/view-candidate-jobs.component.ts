import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../../model/candidate";

@Component({
  selector: 'app-view-candidate-jobs',
  templateUrl: './view-candidate-jobs.component.html',
  styleUrls: ['./view-candidate-jobs.component.scss']
})
export class ViewCandidateJobsComponent implements OnInit {
  @Input() candidate: Candidate;
  @Input() preview: boolean = false;

  constructor() { }

  ngOnInit(): void {
  }
}

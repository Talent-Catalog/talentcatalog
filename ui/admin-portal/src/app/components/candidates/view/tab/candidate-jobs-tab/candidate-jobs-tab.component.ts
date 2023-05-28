import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../../model/candidate";

@Component({
  selector: 'app-candidate-jobs-tab',
  templateUrl: './candidate-jobs-tab.component.html',
  styleUrls: ['./candidate-jobs-tab.component.scss']
})
export class CandidateJobsTabComponent implements OnInit {

  @Input() candidate: Candidate;

  constructor() { }

  ngOnInit(): void {
  }

}

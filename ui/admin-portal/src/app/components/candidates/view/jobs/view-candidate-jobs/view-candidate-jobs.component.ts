import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../../model/candidate";
import {getCandidateOpportunityStageName} from "../../../../../model/candidate-opportunity";
import {truncate} from "../../../../../util/string"

@Component({
  selector: 'app-view-candidate-jobs',
  templateUrl: './view-candidate-jobs.component.html',
  styleUrls: ['./view-candidate-jobs.component.scss']
})
export class ViewCandidateJobsComponent implements OnInit {
  @Input() candidate: Candidate;

  loading: boolean;
  error;

  constructor() { }

  ngOnInit(): void {
  }

  get getCandidateOpportunityStageName() {
   return getCandidateOpportunityStageName;
  }

  get truncate() {
   return truncate;
  }

}

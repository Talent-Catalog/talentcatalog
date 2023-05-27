import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../../model/candidate";
import {getCandidateOpportunityStageName} from "../../../../../model/candidate-opportunity";

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

  truncate(str: string, num: number) {
    if (str && str.length > num) {
      return str.slice(0, num) + "...";
    } else {
      return str;
    }
  }

}

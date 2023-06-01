import {Component, Input, OnInit} from '@angular/core';
import {
  CandidateOpportunity,
  getCandidateOpportunityStageName
} from "../../../../model/candidate-opportunity";

@Component({
  selector: 'app-candidate-opp-progress',
  templateUrl: './candidate-opp-progress.component.html',
  styleUrls: ['./candidate-opp-progress.component.scss']
})
export class CandidateOppProgressComponent implements OnInit {
  @Input() opp: CandidateOpportunity;

  constructor() { }

  ngOnInit(): void {
  }

  get editable(): boolean {
    //todo Needs logic as who can update an opp.
    return true;
  }

  get getCandidateOpportunityStageName() {
    return getCandidateOpportunityStageName
  }

  editOppProgress() {
    //todo editOppProgress
  }
}

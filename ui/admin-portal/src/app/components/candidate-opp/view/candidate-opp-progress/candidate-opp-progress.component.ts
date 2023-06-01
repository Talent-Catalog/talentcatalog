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
  editable: boolean;

  constructor() { }

  ngOnInit(): void {
  }

  get getCandidateOpportunityStageName() {
    return getCandidateOpportunityStageName
  }

  editOppProgress() {
    //todo editOppProgress
  }
}

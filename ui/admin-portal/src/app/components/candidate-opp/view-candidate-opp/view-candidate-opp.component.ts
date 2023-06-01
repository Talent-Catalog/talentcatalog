import {Component, Input, OnInit} from '@angular/core';
import {
  CandidateOpportunity,
  getCandidateOpportunityStageName
} from "../../../model/candidate-opportunity";

@Component({
  selector: 'app-view-candidate-opp',
  templateUrl: './view-candidate-opp.component.html',
  styleUrls: ['./view-candidate-opp.component.scss']
})
export class ViewCandidateOppComponent implements OnInit {
  @Input() opp: CandidateOpportunity;
  @Input() showBreadcrumb: boolean = true;

  constructor() { }

  ngOnInit(): void {
  }

  get getCandidateOpportunityStageName() {
    return getCandidateOpportunityStageName
  }

  get editable(): boolean {
    //todo Needs logic as who can update an opp.
    return true;
  }

  editOppProgress() {
    //todo editOppProgress
  }

}

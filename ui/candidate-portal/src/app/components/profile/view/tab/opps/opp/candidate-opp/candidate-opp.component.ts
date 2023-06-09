import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Candidate} from "../../../../../../../model/candidate";
import {
  CandidateOpportunity,
  getCandidateOpportunityStageName
} from "../../../../../../../model/candidate-opportunity";

@Component({
  selector: 'app-candidate-opp',
  templateUrl: './candidate-opp.component.html',
  styleUrls: ['./candidate-opp.component.scss']
})
export class CandidateOppComponent implements OnInit {
  @Input() selectedOpp: CandidateOpportunity;
  @Input() candidate: Candidate;
  @Output() back = new EventEmitter();

  constructor() { }

  ngOnInit(): void {
  }

  get getCandidateOpportunityStageName() {
    return getCandidateOpportunityStageName;
  }

  goBack() {
    this.selectedOpp = null;
    this.back.emit();
  }

}

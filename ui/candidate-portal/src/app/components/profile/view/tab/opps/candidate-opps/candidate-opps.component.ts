import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Candidate} from "../../../../../../model/candidate";
import {CandidateOpportunity} from "../../../../../../model/candidate-opportunity";

@Component({
  selector: 'app-candidate-opps',
  templateUrl: './candidate-opps.component.html',
  styleUrls: ['./candidate-opps.component.scss']
})
export class CandidateOppsComponent implements OnInit {
  error: string;
  loading: boolean;
  @Input() candidate: Candidate;
  @Output() refresh = new EventEmitter();

  selectedOpp: CandidateOpportunity;

  constructor() { }

  ngOnInit(): void {
  }

  get opps(): CandidateOpportunity[] {
    return this.candidate?.candidateOpportunities;
  }

  selectOpp(opp: CandidateOpportunity) {
    this.selectedOpp = opp;
  }

  unSelectOpp() {
    this.selectedOpp = null;
    this.refresh.emit();
  }

}

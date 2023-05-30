import {Component, Input, OnInit} from '@angular/core';
import {SearchOppsBy} from "../../../model/base";
import {CandidateOpportunity} from "../../../model/candidate-opportunity";
import {MainSidePanelBase} from "../../util/split/MainSidePanelBase";

@Component({
  selector: 'app-candidate-opps-with-detail',
  templateUrl: './candidate-opps-with-detail.component.html',
  styleUrls: ['./candidate-opps-with-detail.component.scss']
})
export class CandidateOppsWithDetailComponent extends MainSidePanelBase implements OnInit {
  @Input() searchBy: SearchOppsBy;

  //todo Use this to implement the Job tab of a candidate
  @Input() candidateOpps: CandidateOpportunity[];

  error: any;
  loading: boolean;

  selectedOpp: CandidateOpportunity;

  constructor() {
    super(6);
  }

  ngOnInit(): void {
  }

  onOppSelected(opp: CandidateOpportunity) {
    this.selectedOpp = opp;
  }
}

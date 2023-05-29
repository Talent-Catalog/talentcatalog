import {Component, Input, OnInit} from '@angular/core';
import {CandidateOpportunity} from "../../../../model/candidate-opportunity";

@Component({
  selector: 'app-candidate-opp-notes',
  templateUrl: './candidate-opp-notes.component.html',
  styleUrls: ['./candidate-opp-notes.component.scss']
})
export class CandidateOppNotesComponent implements OnInit {
  @Input() opp: CandidateOpportunity;

  constructor() { }

  ngOnInit(): void {
  }

}

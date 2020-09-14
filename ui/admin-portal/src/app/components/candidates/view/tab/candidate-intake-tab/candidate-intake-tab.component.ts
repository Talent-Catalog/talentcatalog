import {Component, Input, OnInit} from '@angular/core';
import {Candidate, CandidateIntakeData} from "../../../../../model/candidate";

@Component({
  selector: 'app-candidate-intake-tab',
  templateUrl: './candidate-intake-tab.component.html',
  styleUrls: ['./candidate-intake-tab.component.scss']
})
export class CandidateIntakeTabComponent implements OnInit {
  @Input() candidate: Candidate;
  candidateIntakeData: CandidateIntakeData;

  constructor() { }

  ngOnInit(): void {
    //todo could load existing candidateIntakeData
  }

}

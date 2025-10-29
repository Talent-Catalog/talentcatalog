import {Component, Input} from '@angular/core';
import {Candidate} from "../../../../../model/candidate";

@Component({
  selector: 'app-candidate-cv-text-tab',
  templateUrl: './candidate-cv-text-tab.component.html',
  styleUrls: ['./candidate-cv-text-tab.component.scss']
})
export class CandidateCvTextTabComponent {
  @Input() candidate: Candidate;
  cvText: string = "John was here with deep dta and logic.";
}

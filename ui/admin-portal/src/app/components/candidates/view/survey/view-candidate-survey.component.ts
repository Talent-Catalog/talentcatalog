import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../model/candidate";

@Component({
  selector: 'app-view-candidate-survey',
  templateUrl: './view-candidate-survey.component.html',
  styleUrls: ['./view-candidate-survey.component.scss']
})
export class ViewCandidateSurveyComponent implements OnInit {

  @Input() candidate: Candidate;

  constructor() { }

  ngOnInit() {
    console.log(this.candidate)
  }

}

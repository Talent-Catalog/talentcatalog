import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../model/candidate";

@Component({
  selector: 'app-view-candidate-additional-info',
  templateUrl: './view-candidate-additional-info.component.html',
  styleUrls: ['./view-candidate-additional-info.component.scss']
})
export class ViewCandidateAdditionalInfoComponent implements OnInit {

  @Input() candidate: Candidate;

  constructor() { }

  ngOnInit() {
  }

}

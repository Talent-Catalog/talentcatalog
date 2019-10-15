import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../../model/candidate";
import {Occupation} from "../../../../../model/occupation";

@Component({
  selector: 'app-view-candidate-experience',
  templateUrl: './view-candidate-experience.component.html',
  styleUrls: ['./view-candidate-experience.component.scss']
})
export class ViewCandidateExperienceComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() occupation: Occupation;
  @Input() editable: boolean;

  loading: boolean;
  error: any;

  constructor() { }

  ngOnInit() {

  }

}

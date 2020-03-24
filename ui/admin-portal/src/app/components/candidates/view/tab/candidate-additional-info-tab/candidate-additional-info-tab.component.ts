import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../../model/candidate";
import {User} from "../../../../../model/user";

@Component({
  selector: 'app-candidate-additional-info-tab',
  templateUrl: './candidate-additional-info-tab.component.html',
  styleUrls: ['./candidate-additional-info-tab.component.scss']
})
export class CandidateAdditionalInfoTabComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() editable: boolean = false;
  @Input() loggedInUser: User;

  constructor() { }

  ngOnInit() {
  }

}

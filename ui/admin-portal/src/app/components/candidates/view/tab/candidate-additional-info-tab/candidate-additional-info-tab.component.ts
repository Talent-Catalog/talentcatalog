import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
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
  @Output() candidateChanged = new EventEmitter();

  constructor() { }

  ngOnInit() {
  }

  onCandidateChanged() {
    this.candidateChanged.emit();
  }
}

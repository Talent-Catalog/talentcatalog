import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Candidate} from "../../../model/candidate";
import {User} from "../../../model/user";

@Component({
  selector: 'app-candidate-search-card',
  templateUrl: './candidate-search-card.component.html',
  styleUrls: ['./candidate-search-card.component.scss']
})
export class CandidateSearchCardComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() loggedInUser: User;

  @Output() onClose = new EventEmitter();

  constructor() { }

  ngOnInit() {

  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      // TODO switch to general tab?
    }
  }

  close() {
    this.onClose.emit();
  }

}

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Candidate} from "../../../../../model/candidate";

@Component({
  selector: 'app-candidate-experience-tab',
  templateUrl: './candidate-experience-tab.component.html',
  styleUrls: ['./candidate-experience-tab.component.scss']
})
export class CandidateExperienceTabComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  loading: boolean;
  error;
  result: Candidate;

  constructor() { }

  ngOnInit() {
    this.error = null;
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.error = null;
      this.loading = true;
      // TODO replace service call to load candidate data
      this.result = this.candidate;
      this.loading = false;
    }
  }

}

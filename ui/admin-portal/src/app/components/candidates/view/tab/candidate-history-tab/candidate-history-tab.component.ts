import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Candidate} from "../../../../../model/candidate";

@Component({
  selector: 'app-candidate-history-tab',
  templateUrl: './candidate-history-tab.component.html',
  styleUrls: ['./candidate-history-tab.component.scss']
})
export class CandidateHistoryTabComponent implements OnInit, OnChanges {

  @Input() characterLimit: number;
  @Input() candidate: Candidate;
  @Input() editable: boolean;
  @Output() onResize = new EventEmitter();



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

  onAddNote($event) {
    // TODO
  }

  resize(){
    this.onResize.emit();
  }
}

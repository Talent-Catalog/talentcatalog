import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Candidate} from '../../../model/candidate';
import {User} from '../../../model/user';
import {FormBuilder, FormGroup} from '@angular/forms';

@Component({
  selector: 'app-candidate-search-card',
  templateUrl: './candidate-search-card.component.html',
  styleUrls: ['./candidate-search-card.component.scss']
})
export class CandidateSearchCardComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() loggedInUser: User;
  @Input() sourceType: string;
  @Input() sourceName: string;

  @Output() onClose = new EventEmitter();

  form: FormGroup;

  constructor(private fb: FormBuilder) { }

  ngOnInit() {
    this.form = this.fb.group({
      contextNotes: [''],
    });
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

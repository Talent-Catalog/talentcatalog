import {Component, Input, OnInit} from '@angular/core';
import {Candidate, CandidateIntakeData} from '../../../../model/candidate';
import {dateString} from '../../../../util/date-adapter/date-adapter';

@Component({
  selector: 'app-confirm-contact',
  templateUrl: './confirm-contact.component.html',
  styleUrls: ['./confirm-contact.component.scss']
})
export class ConfirmContactComponent implements OnInit {
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;

  constructor() { }

  ngOnInit(): void {
  }

  get date(): string {
    return dateString(this.candidate.dob)
  }

}

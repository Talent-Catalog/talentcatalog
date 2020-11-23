import {Component, Input, OnInit} from '@angular/core';
import {Candidate, CandidateIntakeData} from '../../../../model/candidate';

@Component({
  selector: 'app-confirm-visa-contact',
  templateUrl: './confirm-visa-contact.component.html',
  styleUrls: ['./confirm-visa-contact.component.scss']
})
export class ConfirmVisaContactComponent implements OnInit {
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;

  constructor() { }

  ngOnInit(): void {
  }

}

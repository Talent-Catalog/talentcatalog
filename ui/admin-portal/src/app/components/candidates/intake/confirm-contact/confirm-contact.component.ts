import {Component, Input, OnInit} from '@angular/core';
import {Candidate, CandidateIntakeData} from '../../../../model/candidate';

@Component({
  selector: 'app-confirm-contact',
  templateUrl: './confirm-contact.component.html',
  styleUrls: ['./confirm-contact.component.scss']
})
export class ConfirmContactComponent implements OnInit {
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  emailQuestion: string;
  phoneQuestion: string;
  whatsappQuestion: string;

  constructor() { }

  ngOnInit(): void {
    this.emailQuestion = "Email? ";
    this.phoneQuestion = "Phone? ";
    this.whatsappQuestion = "Whatsapp? ";
  }

}

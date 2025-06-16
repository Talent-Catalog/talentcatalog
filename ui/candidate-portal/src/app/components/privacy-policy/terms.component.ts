import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-terms',
  templateUrl: './terms.component.html',
  styleUrls: ['./terms.component.scss']
})
export class TermsComponent implements OnInit {
  content: string;
  partnerName: string;
  requestAcceptance: boolean;
  readTerms: boolean = false;

  ngOnInit(): void {
    //todo Load candidates approved policy - if latest requestAcceptance is false, otherwise
    //set tag pendingTermsAcceptance, load latest policy and requestAcceptance = true
    this.partnerName = "TBB"
    this.content = "Whatever you want baby"
    this.requestAcceptance = false;
  }

  setReadTerms() {
    this.readTerms = true;
  }
}

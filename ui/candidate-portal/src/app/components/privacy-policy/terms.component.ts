import {Component, Input, OnInit} from '@angular/core';
import {TermsInfo, TermsType} from "../../model/terms-info";

@Component({
  selector: 'app-terms',
  templateUrl: './terms.component.html',
  styleUrls: ['./terms.component.scss']
})
export class TermsComponent implements OnInit {
  @Input() content: string;
  @Input() partnerName: string = "TestPartner???";

  currentPrivacyPolicy: TermsInfo;

  ngOnInit(): void {
  }

  get TermsType() {
    return TermsType;
  }
}

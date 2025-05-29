import {Component, Input, OnInit} from '@angular/core';
import {TermsInfoDto, TermsType} from "../../model/terms-info-dto";

@Component({
  selector: 'app-terms',
  templateUrl: './terms.component.html',
  styleUrls: ['./terms.component.scss']
})
export class TermsComponent implements OnInit {
  @Input() content: string;
  @Input() partnerName: string = "TestPartner???";

  currentPrivacyPolicy: TermsInfoDto;

  ngOnInit(): void {
  }

  get TermsType() {
    return TermsType;
  }
}

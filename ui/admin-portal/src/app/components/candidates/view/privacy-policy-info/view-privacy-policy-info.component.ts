import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../model/candidate";

@Component({
  selector: 'app-view-privacy-policy-info',
  templateUrl: './view-privacy-policy-info.component.html',
  styleUrls: ['./view-privacy-policy-info.component.scss']
})
export class ViewPrivacyPolicyInfoComponent implements OnInit {
  @Input() candidate: Candidate;
  @Input() editable: boolean;

  constructor() { }

  ngOnInit() {

  }
}

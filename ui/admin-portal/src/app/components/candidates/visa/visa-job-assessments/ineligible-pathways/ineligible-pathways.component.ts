import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder} from "@angular/forms";
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-ineligible-pathways',
  templateUrl: './ineligible-pathways.component.html',
  styleUrls: ['./ineligible-pathways.component.scss']
})
export class IneligiblePathwaysComponent extends VisaCheckComponentBase implements OnInit {

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobIneligiblePathways: [this.visaJobCheck?.ineligiblePathways],
    });
  }

}

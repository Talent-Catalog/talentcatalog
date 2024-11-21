import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder} from "@angular/forms";
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-eligible-pathways',
  templateUrl: './eligible-pathways.component.html',
  styleUrls: ['./eligible-pathways.component.scss']
})
export class EligiblePathwaysComponent extends VisaCheckComponentBase implements OnInit {

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobEligiblePathways: [this.visaJobCheck?.eligiblePathways],
    });
  }

}

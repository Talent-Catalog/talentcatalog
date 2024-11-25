import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder} from "@angular/forms";
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-relevant-work-exp',
  templateUrl: './relevant-work-exp.component.html',
  styleUrls: ['./relevant-work-exp.component.scss']
})
export class RelevantWorkExpComponent extends VisaCheckComponentBase implements OnInit {

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobRelevantWorkExp: [this.visaJobCheck?.relevantWorkExp]
    });
  }
}

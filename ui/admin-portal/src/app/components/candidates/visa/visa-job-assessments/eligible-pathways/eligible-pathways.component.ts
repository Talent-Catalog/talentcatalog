import {Component, Input, OnInit} from '@angular/core';
import {CandidateVisaJobCheck} from "../../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-eligible-pathways',
  templateUrl: './eligible-pathways.component.html',
  styleUrls: ['./eligible-pathways.component.scss']
})
export class EligiblePathwaysComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedJobCheck: CandidateVisaJobCheck;

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.selectedJobCheck?.id],
      visaJobEligiblePathways: [this.selectedJobCheck?.eligiblePathways],
    });
  }

}

import {Component, Input, OnInit} from '@angular/core';
import {CandidateVisaJobCheck} from "../../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-relevant-work-exp',
  templateUrl: './relevant-work-exp.component.html',
  styleUrls: ['./relevant-work-exp.component.scss']
})
export class RelevantWorkExpComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedJobCheck: CandidateVisaJobCheck;

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.selectedJobCheck?.id],
      visaJobWorkExp: [this.selectedJobCheck?.workExp]
    });
  }
}

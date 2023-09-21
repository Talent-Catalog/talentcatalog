import {Component, Input, OnInit} from '@angular/core';
import {CandidateVisaJobCheck} from "../../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-visa-job-notes',
  templateUrl: './visa-job-notes.component.html',
  styleUrls: ['./visa-job-notes.component.scss']
})
export class VisaJobNotesComponent extends VisaCheckComponentBase implements OnInit {

  @Input() selectedJobCheck: CandidateVisaJobCheck;

  constructor(fb: FormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobNotes: [this.visaJobCheck?.notes],
    });
  }

}

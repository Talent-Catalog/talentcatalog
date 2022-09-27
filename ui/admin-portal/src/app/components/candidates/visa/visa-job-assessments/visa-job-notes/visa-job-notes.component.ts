import {Component, Input, OnInit} from '@angular/core';
import {CandidateVisaJobCheck} from "../../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-visa-job-notes',
  templateUrl: './visa-job-notes.component.html',
  styleUrls: ['./visa-job-notes.component.scss']
})
export class VisaJobNotesComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedJobCheck: CandidateVisaJobCheck;

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.selectedJobCheck?.id],
      visaJobNotes: [this.selectedJobCheck?.notes],
    });
  }

}

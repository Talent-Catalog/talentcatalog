import {Component, Input, OnInit} from '@angular/core';
import {CandidateVisaJobCheck} from "../../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-preferred-pathways',
  templateUrl: './preferred-pathways.component.html',
  styleUrls: ['./preferred-pathways.component.scss']
})
export class PreferredPathwaysComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedJobCheck: CandidateVisaJobCheck;
  helpLink: string;

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.selectedJobCheck?.id],
      visaJobPreferredPathways: [this.selectedJobCheck?.preferredPathways],
    });
  }

}

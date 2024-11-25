import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder} from "@angular/forms";
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-preferred-pathways',
  templateUrl: './preferred-pathways.component.html',
  styleUrls: ['./preferred-pathways.component.scss']
})
export class PreferredPathwaysComponent extends VisaCheckComponentBase implements OnInit {
  helpLink: string;

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobPreferredPathways: [this.visaJobCheck?.preferredPathways],
    });
  }

}

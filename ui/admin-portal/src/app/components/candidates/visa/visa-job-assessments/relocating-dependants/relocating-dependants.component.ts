import {Component, Input, OnInit} from '@angular/core';
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {FormBuilder} from "@angular/forms";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";
import {CandidateDependant,} from "../../../../../model/candidate";

@Component({
  selector: 'app-relocating-dependants',
  templateUrl: './relocating-dependants.component.html',
  styleUrls: ['./relocating-dependants.component.scss']
})
export class RelocatingDependantsComponent extends VisaCheckComponentBase implements OnInit {

  @Input() dependants: CandidateDependant[];

  constructor(fb: FormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobRelocatingDependantIds: [this.visaJobCheck?.relocatingDependantIds],
    });
  }
}

import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../../util/enum";
import {YesNo} from "../../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";

@Component({
  selector: 'app-job-english-threshold',
  templateUrl: './job-english-threshold.component.html',
  styleUrls: ['./job-english-threshold.component.scss']
})
export class JobEnglishThresholdComponent extends VisaCheckComponentBase implements OnInit {
  //Drop down values for enumeration
  englishThresholdOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobEnglishThreshold: [this.visaJobCheck?.englishThreshold],
      visaJobEnglishThresholdNotes: [this.visaJobCheck?.englishThresholdNotes],
    });
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.visaJobEnglishThreshold) {
      if (this.form.value.visaJobEnglishThreshold === 'Yes') {
        found = true
      }
      if (this.form.value.visaJobEnglishThreshold === 'No') {
        found = true
      }
    }
    return found;
  }
}

import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {VisaCheckComponentBase} from "../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-english-threshold',
  templateUrl: './english-threshold.component.html',
  styleUrls: ['./english-threshold.component.scss']
})
export class EnglishThresholdComponent extends VisaCheckComponentBase implements OnInit {
  //Drop down values for enumeration
  englishThresholdOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheck?.id],
      visaCountryId: [this.visaCheck?.country?.id],
      visaEnglishThreshold: [this.visaCheck?.englishThreshold],
      visaEnglishThresholdNotes: [this.visaCheck?.englishThresholdNotes],
    });
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.visaEnglishThreshold) {
      if (this.form.value.visaEnglishThreshold === 'Yes') {
        found = true
      }
      if (this.form.value.visaEnglishThreshold === 'No') {
        found = true
      }
    }
    return found;
  }

}

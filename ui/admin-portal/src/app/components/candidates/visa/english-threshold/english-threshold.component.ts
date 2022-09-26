import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";
import {UntypedFormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-english-threshold',
  templateUrl: './english-threshold.component.html',
  styleUrls: ['./english-threshold.component.scss']
})
export class EnglishThresholdComponent extends IntakeComponentBase implements OnInit {
  //Drop down values for enumeration
  englishThresholdOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheckRecord?.id],
      visaCountryId: [this.visaCheckRecord?.country?.id],
      visaEnglishThreshold: [this.visaCheckRecord?.englishThreshold],
      visaEnglishThresholdNotes: [this.visaCheckRecord?.englishThresholdNotes],
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

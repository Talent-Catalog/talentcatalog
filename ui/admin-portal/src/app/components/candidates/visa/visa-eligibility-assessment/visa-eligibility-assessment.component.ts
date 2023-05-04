import {Component, OnInit} from '@angular/core';
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";

@Component({
  selector: 'app-visa-eligibility-assessment',
  templateUrl: './visa-eligibility-assessment.component.html',
  styleUrls: ['./visa-eligibility-assessment.component.scss']
})
export class VisaEligibilityAssessmentComponent extends IntakeComponentBase implements OnInit {
  visaEligibilityAssessmentOptions: EnumOption[] = enumOptions(YesNo);
  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheckRecord?.id],
      visaCountryId: [this.visaCheckRecord?.country.id],
      visaEligibilityAssessment: [this.visaCheckRecord?.visaEligibilityAssessment],
    });
  }

}

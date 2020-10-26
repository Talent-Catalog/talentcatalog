/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Component, OnInit} from '@angular/core';
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {CandidateVisaCheck, VisaEligibility} from "../../../../model/candidate";
import {EnumOption, enumOptions} from "../../../../util/enum";

@Component({
  selector: 'app-visa-assessment',
  templateUrl: './visa-assessment.component.html',
  styleUrls: ['./visa-assessment.component.scss']
})
export class VisaAssessmentComponent extends IntakeComponentBase implements OnInit {

  //Drop down values for enumeration
  visaEligibilityOptions: EnumOption[] = enumOptions(VisaEligibility);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.myRecord?.id],
      visaCountryId: [this.myRecord?.country?.id],
      visaEligibility: [this.myRecord?.eligibility],
      visaAssessmentNotes: [this.myRecord?.assessmentNotes],
    });
  }

  private get myRecord(): CandidateVisaCheck {
    return this.candidateIntakeData.candidateVisaChecks ?
      this.candidateIntakeData.candidateVisaChecks[this.myRecordIndex]
      : null;
  }

}

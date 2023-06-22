import {Component, OnInit} from '@angular/core';
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";

@Component({
  selector: 'app-pathway-assessment',
  templateUrl: './pathway-assessment.component.html',
  styleUrls: ['./pathway-assessment.component.scss']
})
export class PathwayAssessmentComponent extends IntakeComponentBase implements OnInit {

//Drop down values for enumeration
  assessmentOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.visaCheckRecord?.id],
      visaCountryId: [this.visaCheckRecord?.country?.id],
      visaPathwayAssessment: [this.visaCheckRecord?.pathwayAssessment],
    });
  }

}

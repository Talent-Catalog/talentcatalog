import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {TBBEligibilityAssessment} from '../../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-job-eligibility-assessment',
  templateUrl: './job-eligibility-assessment.component.html',
  styleUrls: ['./job-eligibility-assessment.component.scss']
})
export class JobEligibilityAssessmentComponent extends VisaCheckComponentBase implements OnInit {

  public jobEligibilityAssessOptions: EnumOption[] = enumOptions(TBBEligibilityAssessment);

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobTbbEligibility: [this.visaJobCheck?.tbbEligibility],
    });
  }

}

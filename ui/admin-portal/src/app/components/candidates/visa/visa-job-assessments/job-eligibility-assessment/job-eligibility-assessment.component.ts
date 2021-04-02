import {Component, Input, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {CandidateVisaJobCheck, TBBEligibilityAssessment} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-job-eligibility-assessment',
  templateUrl: './job-eligibility-assessment.component.html',
  styleUrls: ['./job-eligibility-assessment.component.scss']
})
export class JobEligibilityAssessmentComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedIndex: number;
  @Input() selectedJobCheck: CandidateVisaJobCheck;
  public jobEligibilityAssessOptions: EnumOption[] = enumOptions(TBBEligibilityAssessment);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.selectedJobCheck?.id],
      visaJobTbbEligibility: [this.selectedJobCheck?.tbbEligibility],
    });
  }

}

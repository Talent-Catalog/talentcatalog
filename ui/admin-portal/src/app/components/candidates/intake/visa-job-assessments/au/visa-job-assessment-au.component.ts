import {Component, Input, OnInit} from '@angular/core';
import {IntakeComponentTabBase} from '../../../../util/intake/IntakeComponentTabBase';
import {CandidateJobCheck, CandidateVisaCheck} from '../../../../../model/candidate';

@Component({
  selector: 'app-visa-job-assessment-au',
  templateUrl: './visa-job-assessment-au.component.html',
  styleUrls: ['./visa-job-assessment-au.component.scss']
})
export class VisaJobAssessmentAuComponent extends IntakeComponentTabBase implements OnInit {
  @Input() jobIndex: number;
  @Input() visaRecord: CandidateVisaCheck;

  get currentYear(): string {
    return new Date().getFullYear().toString();
  }

  get birthYear(): string {
    return this.candidate.dob.toString().slice(0, 4);
  }

  get myRecord(): CandidateJobCheck {
    return this.visaRecord.jobChecks ? this.visaRecord.jobChecks[this.jobIndex] : null;
  }


}

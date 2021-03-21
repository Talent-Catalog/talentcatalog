import {Component, Input, OnInit} from '@angular/core';
import {CandidateVisa, CandidateVisaJob} from '../../../../../model/candidate';
import {IntakeComponentTabBase} from '../../../../util/intake/IntakeComponentTabBase';

@Component({
  selector: 'app-visa-job-assessment-ca',
  templateUrl: './visa-job-assessment-ca.component.html',
  styleUrls: ['./visa-job-assessment-ca.component.scss']
})
export class VisaJobAssessmentCaComponent extends IntakeComponentTabBase implements OnInit {

  @Input() jobIndex: number;
  @Input() visaRecord: CandidateVisa;

  get currentYear(): string {
    return new Date().getFullYear().toString();
  }

  get birthYear(): string {
    return this.candidate.dob.toString().slice(0, 4);
  }

  get myRecord(): CandidateVisaJob {
    return this.visaRecord.jobChecks ? this.visaRecord.jobChecks[this.jobIndex] : null;
  }
}

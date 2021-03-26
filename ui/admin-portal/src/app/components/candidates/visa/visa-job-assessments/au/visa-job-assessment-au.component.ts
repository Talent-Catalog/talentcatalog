import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {IntakeComponentTabBase} from '../../../../util/intake/IntakeComponentTabBase';
import {CandidateVisa, CandidateVisaJobCheck} from '../../../../../model/candidate';

@Component({
  selector: 'app-visa-job-assessment-au',
  templateUrl: './visa-job-assessment-au.component.html',
  styleUrls: ['./visa-job-assessment-au.component.scss']
})
export class VisaJobAssessmentAuComponent extends IntakeComponentTabBase implements OnInit, OnChanges {
  @Input() jobIndex: number;
  @Input() visaRecord: CandidateVisa;
  @Input() selectedJobCheck: CandidateVisaJobCheck;

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.selectedJobCheck && changes.selectedJobCheck.previousValue !== changes.selectedJobCheck.currentValue) {
      this.selectedJobCheck = changes.selectedJobCheck.currentValue;
    }
  }

  get currentYear(): string {
    return new Date().getFullYear().toString();
  }

  get birthYear(): string {
    return this.candidate.dob.toString().slice(0, 4);
  }

  // changeCheck(jobCheck: CandidateVisaJobCheck) {
  //   this.selectedJobCheck = jobCheck;
  // }

}

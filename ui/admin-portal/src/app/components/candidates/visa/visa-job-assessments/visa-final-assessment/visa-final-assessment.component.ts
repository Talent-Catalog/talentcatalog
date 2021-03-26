import {Component, Input} from '@angular/core';
import {IntakeComponentTabBase} from '../../../../util/intake/IntakeComponentTabBase';
import {CandidateVisaJobCheck} from "../../../../../model/candidate";

@Component({
  selector: 'app-visa-final-assessment',
  templateUrl: './visa-final-assessment.component.html',
  styleUrls: ['./visa-final-assessment.component.scss']
})
export class VisaFinalAssessmentComponent extends IntakeComponentTabBase {
  @Input() selectedIndex: number;
  @Input() visaCheckRecord: CandidateVisaJobCheck;
  @Input() selectedJobCheck: CandidateVisaJobCheck;
}

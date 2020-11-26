import {Component, Input} from '@angular/core';
import {IntakeComponentTabBase} from '../../../../util/intake/IntakeComponentTabBase';

@Component({
  selector: 'app-visa-job-assessment',
  templateUrl: './visa-job-assessment.component.html',
  styleUrls: ['./visa-job-assessment.component.scss']
})
export class VisaJobAssessmentComponent extends IntakeComponentTabBase {
  @Input() selectedIndex: number;
}

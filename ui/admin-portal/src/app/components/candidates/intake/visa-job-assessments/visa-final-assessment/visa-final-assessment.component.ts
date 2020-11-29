import {Component, Input} from '@angular/core';
import {IntakeComponentTabBase} from '../../../../util/intake/IntakeComponentTabBase';

@Component({
  selector: 'app-visa-final-assessment',
  templateUrl: './visa-final-assessment.component.html',
  styleUrls: ['./visa-final-assessment.component.scss']
})
export class VisaFinalAssessmentComponent extends IntakeComponentTabBase {
  @Input() selectedIndex: number;
}

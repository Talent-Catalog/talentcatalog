import {Component, Input} from '@angular/core';
import {IntakeComponentTabBase} from "../../../../../util/intake/IntakeComponentTabBase";

@Component({
  selector: 'app-visa-check-uk',
  templateUrl: './visa-check-uk.component.html',
  styleUrls: ['./visa-check-uk.component.scss']
})
export class VisaCheckUkComponent extends IntakeComponentTabBase {
  @Input() selectedIndex: number;
}


import {Component, Input} from '@angular/core';
import {IntakeComponentTabBase} from "../../../../../util/intake/IntakeComponentTabBase";

@Component({
  selector: 'app-visa-check-au',
  templateUrl: './visa-check-au.component.html',
  styleUrls: ['./visa-check-au.component.scss']
})
export class VisaCheckAuComponent extends IntakeComponentTabBase {
  @Input() selectedIndex: number;
}

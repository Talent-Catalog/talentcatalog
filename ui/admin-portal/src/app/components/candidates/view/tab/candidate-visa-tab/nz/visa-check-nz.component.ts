import {Component, Input} from '@angular/core';
import {IntakeComponentTabBase} from "../../../../../util/intake/IntakeComponentTabBase";

@Component({
  selector: 'app-visa-check-nz',
  templateUrl: './visa-check-nz.component.html',
  styleUrls: ['./visa-check-nz.component.scss']
})
export class VisaCheckNzComponent extends IntakeComponentTabBase {
  @Input() selectedIndex: number;
}

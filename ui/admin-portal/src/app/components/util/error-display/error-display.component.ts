import {Component, Input} from '@angular/core';

/**
 * Reusable component to display errors received from parent
 */
@Component({
  selector: 'app-error-display',
  templateUrl: './error-display.component.html',
  styleUrls: ['./error-display.component.scss']
})
export class ErrorDisplayComponent {
  @Input("error") error: any;
}

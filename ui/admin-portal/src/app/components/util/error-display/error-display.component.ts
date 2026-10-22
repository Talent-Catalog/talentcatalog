import {Component, Input, ChangeDetectionStrategy} from '@angular/core';

/**
 * Reusable component to display errors received from parent
 */
@Component({
  selector: 'app-error-display',
  templateUrl: './error-display.component.html',
  styleUrls: ['./error-display.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class ErrorDisplayComponent {
  @Input("error") error: any;
}

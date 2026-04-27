import {Component, Input} from '@angular/core';

/**
 * Reusable component to display loading status of parent - uses translation key and provides option
 * to retain an empty line when parent not loading, so that view doesn't bounce around when it is.
 */
@Component({
  selector: 'app-loading-indicator',
  templateUrl: './loading-indicator.component.html',
  styleUrls: ['./loading-indicator.component.scss']
})
export class LoadingIndicatorComponent {
  @Input() loading: boolean = false;
  /**
   * Set to true to retain an empty line when not loading
   */
  @Input() holdTheLine: boolean;
}

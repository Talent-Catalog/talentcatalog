import {Component, Input} from '@angular/core';

/**
 * @component LoadingComponent
 * @selector tc-loading
 * @description
 * A design-system loading indicator that displays a pulsing logo inside a ring.
 * Can be used for full-page loading states or smaller section loaders.
 *
 * **Features**
 * - Two visual types:
 *   - `page`: large, centered overlay with dimmed background
 *   - `section`: small inline loader for partial content
 * - Animated pulsing ring with logo inside
 * - Optional animated dots after the text
 * - Accessibility support with screen-reader label
 * - Optional `holdTheLine` spacer to prevent layout shift when hidden
 *
 * **Inputs**
 * - `loading: boolean`
 *   Controls visibility of the loader. Defaults to `false`.
 * - `type: 'page' | 'section'`
 *   Visual variant. Defaults to `'section'`.
 * - `holdTheLine: boolean`
 *   Keeps a blank line when loader is not active. Defaults to `false`.
 *
 * @examples
 * ```html
 * <!-- Section loader (default) -->
 * <tc-loading [loading]="true"></tc-loading>
 *
 * <!-- Page loader -->
 * <tc-loading [loading]="true" type="page"></tc-loading>
 */

@Component({
  selector: 'tc-loading',
  templateUrl: './tc-loading.component.html',
  styleUrls: ['./tc-loading.component.scss']
})
export class TcLoadingComponent {
  @Input() loading: boolean = false;
  @Input() type: 'page' | 'section' = 'section';
  /**
   * Set to true to retain an empty line when not loading
   */
  @Input() holdTheLine: boolean;
}

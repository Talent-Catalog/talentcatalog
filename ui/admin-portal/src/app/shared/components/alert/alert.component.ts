import {Component, EventEmitter, Input, Output} from '@angular/core';

/**
 * @component AlertComponent
 * @selector tc-alert
 * @description
 * A design-system wrapper around `ngb-alert` that provides consistent styling,
 * optional inline icons, and a smarter dismiss behavior:
 * the close button (×) is shown only if you either bind the `(closed)` output
 * or explicitly set `[dismissible]="true"`.
 *
 * This component uses Font Awesome `<i>` icons internally (not inline SVG); all icons inherit color via `currentColor`.
 *
 * **How it works**
 * - Applies your `.tc-alert` styles and variant token mixins.
 * - Icons are inline SVGs using `fill="currentColor"` so they inherit the variant color.
 * - Dismissible logic:
 *   - If `[dismissible]` is **unset**, the × appears only when `(closed)` is bound.
 *   - If `[dismissible]="true"`, the × is forced on.
 *   - If `[dismissible]="false"`, the × is hidden even if `(closed)` is bound.
 *
 * **Inputs**
 * - `type: 'success' | 'info' | 'warning' | 'danger' | 'primary' | 'secondary' | 'light' | 'dark'`
 *   The visual variant. Defaults to `'warning'`.
 * - `animation: boolean`
 *   Enable/disable close animation. Defaults to `true`.
 * - `dismissible?: boolean`
 *   Force showing/hiding the close button. If omitted, it’s auto based on `(closed)` subscription.
 * - `showIcon: boolean`
 *   Show/hide the inline icon. Defaults to `true`.
 * - `icon?: string`
 *   Optional Bootstrap Icons class name override (e.g., `'bi-x-circle-fill'`). If not provided,
 *   a sensible default per `type` is used. (Note: inline SVG path controls the actual shape.)
 * - `ariaLabel?: string`
 *   Accessible label for the icon (e.g., `"Error:"`, `"Success:"`). If omitted, a default
 *   label is chosen based on `type`.
 * - `show: boolean`
 *   Controls visibility. Defaults to `true`. Set to `false` to remove the alert from the DOM.
 *
 * **Outputs**
 *  - `(closed): EventEmitter<void>`
 *    Emits when the user clicks the close (×) or when the component is programmatically closed.
 *    The component then sets `show = false` internally.
 *
 *
 * @examples
 * ```html
 * <!-- 1) Simple success (dismissible auto OFF because no (closed) bound) -->
 * <tc-alert type="success">
 *   <strong>Success!</strong> Your profile has been updated.
 * </tc-alert>
 *
 * <!-- 2) Warning with description (non-dismissible) -->
 * <tc-alert type="warning" [dismissible]="false">
 *   <div><strong>Warning:</strong> Subscription is expiring soon.</div>
 *   <div>Please update your payment method to avoid interruption.
 *     <a href="https://example.com/billing" target="_blank" rel="noopener noreferrer">Manage billing</a>
 *   </div>
 * </tc-alert>
 *
 * <!-- 3) Danger with list (dismissible auto ON because (closed) is bound) -->
 * <tc-alert type="danger" (closed)="onClose()">
 *   <div><strong>Error Here</strong></div>
 *   <ul>
 *     <li>Your password must be at least 8 characters.</li>
 *     <li>Your password must include at least one number.</li>
 *   </ul>
 * </tc-alert>
 *
 * <!-- 4) Info with icon hidden -->
 * <tc-alert type="info" [showIcon]="false">
 *   <strong>Info:</strong> System maintenance at 02:00.
 * </tc-alert>
 * ```
 */
@Component({
  selector: 'tc-alert',
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.scss'],
})
export class AlertComponent {
  @Input() type: 'success' | 'info' | 'warning' | 'danger' | 'primary' | 'secondary' | 'light' | 'dark' = 'warning';
  @Input() animation = true;
  @Input() icon?: string; // Optional custom icon class
  @Input() showIcon = true;
  @Input() ariaLabel?: string;
  @Input() dismissible?: boolean;
  @Input() show = true;
  @Output() closed = new EventEmitter<void>();


  get classList(): string[] {
    return ['tc-alert'];
  }

  /** Final value used by <ngb-alert>. If no override, show × only when there are subscribers to (closed). */
  get IsDismissible(): boolean {
    return this.dismissible ?? (this.closed.observers.length > 0);
  }

  get defaultIcon(): string {
    const iconMap = {
      success: 'bi-check-circle-fill',
      info: 'bi-info-circle-fill',
      warning: 'bi-exclamation-triangle-fill',
      danger: 'bi-x-circle-fill',
      primary: 'bi-info-circle-fill',
      secondary: 'bi-info-circle-fill',
      light: 'bi-info-circle-fill',
      dark: 'bi-info-circle-fill',
    };
    return this.icon || iconMap[this.type];
  }

  get ariaLabelValue(): string {
    if (this.ariaLabel) {
      return this.ariaLabel;
    }

    const labelMap = {
      success: 'Success:',
      info: 'Info:',
      warning: 'Warning:',
      danger: 'Error:',
      primary: 'Notice:',
      secondary: 'Notice:',
      light: 'Notice:',
      dark: 'Notice:',
    };
    return labelMap[this.type];
  }

  onClose(): void {
    this.show = false;
    this.closed.emit();
  }
}

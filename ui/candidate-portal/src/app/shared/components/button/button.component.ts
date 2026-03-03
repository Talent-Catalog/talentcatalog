import {Component, EventEmitter, HostBinding, Input, Output} from '@angular/core';
import {QueryParamsHandling} from '@angular/router';

/**
 * @component ButtonComponent
 * @selector tc-button
 * @description
 * A design-system button that wraps a native `<button>` element and applies
 * consistent sizing, visual styling, and accessibility behavior. Content is
 * projected via `<ng-content>` so you can place text, icons, or both.
 *
 * **Features**
 * - Size presets: `xs`, `sm`, `default`, `lg`, `xl`
 * - Visual types: `solid`, `outline`, `plain`
 * - Color options: `primary`, `secondary`, `success`, `warning`, `error`, `info`, `gray`
 * - Disabled state styling and pointer lock
 * - Focus-visible outline for keyboard accessibility
 * - Optional `ariaLabel` for icon-only buttons
 * - Optional Angular `routerLink` and `queryParamsHandling` support for navigation
 *
 * **Inputs**
 * - `size: 'xs' | 'sm' | 'default' | 'lg' | 'xl'`
 *   Controls height and padding. Defaults to `'default'`.
 * - `type: 'solid' | 'outline' | 'plain'`
 *   Controls the visual treatment of the button. Defaults to `'solid'`.
 * - `color: 'primary' | 'secondary' | 'success' | 'warning' | 'error' | 'info' | 'gray'`
 *   Controls the button color within the selected `type`. Defaults to `'primary'`.
 * - `disabled: boolean`
 *   Disables the button and applies muted styling. Defaults to `false`.
 * - `ariaLabel?: string`
 *   Accessible label for icon-only or ambiguous buttons.
 * - `routerLink?: string | any[]`
 *   Optional Angular Router `routerLink` for navigation.
 * - `queryParamsHandling?: QueryParamsHandling`
 *   Optional Angular Router query param handling mode used with `routerLink`.
 * - `stopNativeClickPropagation: boolean`
 *   Stops the native click event from bubbling by default. Defaults to `true`.
 *
 * **Outputs**
 * - `(onClick)`
 *   Emits when the internal native button is clicked.
 *   This component exposes `(onClick)` instead of a declared `click` output to
 *   avoid Angular and IDE type-checking errors on `<tc-button>`.
 *
 * @examples
 * ```html
 * <!-- Primary solid (default size) -->
 * <tc-button>Save changes</tc-button>
 *
 * <!-- Secondary solid, large -->
 * <tc-button color="secondary" size="lg">Continue</tc-button>
 *
 * <!-- Secondary outline, small -->
 * <tc-button type="outline" color="secondary" size="sm">Learn more</tc-button>
 *
 * <!-- Plain (text-only look) -->
 * <tc-button type="plain">Cancel</tc-button>
 *
 * <!-- Disabled -->
 * <tc-button [disabled]="true">Processing...</tc-button>
 *
 * <!-- Router navigation with preserved query params -->
 * <tc-button [routerLink]="'/register'" [queryParamsHandling]="'merge'">
 *   Register
 * </tc-button>
 * ```
 */

@Component({
  selector: 'tc-button',
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.scss'],
})
export class ButtonComponent {
  @Input() size: 'xs' | 'sm' | 'default' | 'lg' | 'xl'  = 'default';
  @Input() type: 'solid' | 'outline' | 'plain' = 'solid';
  @Input() color: 'primary' | 'secondary' | 'success' | 'warning' | 'error' | 'info' | 'gray'= 'primary';
  @Input() disabled = false;
  @Input() ariaLabel?: string;
  @Input() routerLink?: string | any[];
  @Input() queryParamsHandling?: QueryParamsHandling;
  /**
   * Whether to prevent the native `click` event from bubbling up the DOM.
   *
   * By default, this is `true`, which means the component calls
   * `event.stopPropagation()` when clicked. This prevents unintended side
   * effects in contexts like accordion headers, nested buttons, or clickable
   * containers.
   *
   * Set this to `false` if you need the native click to bubble — for example,
   * when using `<tc-button>` as a trigger for directives that depend on the
   * native event, such as `ngbDropdownToggle` or other third-party UI controls.
   *
   * @default true
   */
  @Input() stopNativeClickPropagation: boolean = true;

  @Output() onClick = new EventEmitter();

  @HostBinding('class.disabled') get isDisabled() {
    return this.disabled;
  }

  get classList(): string[] {
    return [
      `btn-${this.size}`,
      `btn-${this.color}`,
      `btn-${this.type}`,
    ];
  }

  clicked(e: MouseEvent): void {
    if (this.stopNativeClickPropagation) {
      e.stopPropagation();
    }
    this.onClick.emit();
  }

}

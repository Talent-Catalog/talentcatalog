import {Component, EventEmitter, HostBinding, Input, Output} from '@angular/core';

/**
 * @component ButtonComponent
 * @selector tc-button
 * @description
 * A design-system button that renders as a native `<button>` or `<a>` and applies
 * consistent sizing, variants, and accessibility. Content is projected via
 * `<ng-content>` so you can place text, icons, or both.
 *
 * **Features**
 * - Size presets: `xs`, `sm`, `default`, `lg`, `xl`
 * - Visual variants: `solid`, `outline`, `plain`
 * - Disabled state styling & pointer-lock
 * - Focus-visible outline for keyboard accessibility
 * - Optional `ariaLabel` for icon-only buttons
 * - Optional Angular `routerLink` support for navigation
 * - Optional external link support via `href`, `target`, and `rel`
 *
 * **Inputs**
 * - `size: 'xs' | 'sm' | 'default' | 'lg' | 'xl'`
 *   Controls height and padding. Defaults to `'default'`.
 * - `type: 'solid' | 'outline' | 'plain'`
 *   Visual style variant. Defaults to `'solid'`.
 *   - `color: 'primary' | 'secondary' | 'success' | 'warning' | 'error' | 'info' | 'gray'`
 *   Controls the button’s color scheme. Defaults to `'primary'`.
 * - `disabled: boolean`
 *   Disables the button and applies muted styling. Defaults to `false`.
 * - `ariaLabel?: string`
 *   Accessible label for icon-only or ambiguous buttons.
 * - `routerLink?: string | any[]`
 *   Optional Angular Router `routerLink` for internal app navigation.
 * - `href?: string`
 *   Optional external link URL. When provided, the component renders an `<a>`.
 * - `target?: string`
 *   Optional link target when `href` is provided (eg `_blank`).
 * - `rel?: string`
 *   Optional link rel attribute. If omitted and `target="_blank"`, defaults to
 *   `noopener noreferrer`.
 *
 * **Outputs**
 * - `(onClick)`
 * Instead of re-emitting the native (click) event, this component provides its own (onClick) output.
 * Using (click) directly on <tc-button> works at runtime (because the event bubbles), but IDE
 * type-checking flags it as invalid since Angular can’t see a declared @Output('click').
 * To avoid false errors in IntelliJ/Angular Language Service, we use (onClick) as the explicit output.
 *
 * @examples
 * ```html
 * <!-- Primary (default size) -->
 * <tc-button type="solid">Save changes</tc-button>
 *
 * <!-- Secondary, large -->
 * <tc-button color="secondary" size="lg">Continue</tc-button>
 *
 * <!-- Outline, small -->
 * <tc-button type="outline" size="sm">Learn more</tc-button>
 *
 * <!-- Plain (text-only look) -->
 * <tc-button type="plain">Cancel</tc-button>
 *
 * <!-- Disabled -->
 * <tc-button type="solid" [disabled]="true">Processing…</tc-button>
 *
 * <!-- External link -->
 * <tc-button color="secondary" href="https://example.com" target="_blank">
 *   Open docs
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
  @Input() href?: string;
  @Input() target?: string;
  @Input() rel?: string;
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

  get isLink(): boolean {
    return !!this.href;
  }

  get computedRel(): string | null {
    if (this.rel) {
      return this.rel;
    }
    return this.target === '_blank' ? 'noopener noreferrer' : null;
  }

  clicked(e: MouseEvent): void {
    if (this.disabled) {
      e.preventDefault();
      e.stopPropagation();
      return;
    }

    if (this.stopNativeClickPropagation) {
      e.stopPropagation();
    }
    this.onClick.emit();
  }

}

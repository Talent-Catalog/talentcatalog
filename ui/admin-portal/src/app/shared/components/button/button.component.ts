import {Component, EventEmitter, HostBinding, Input, Output} from '@angular/core';

/**
 * @component ButtonComponent
 * @selector tc-button
 * @description
 * A design-system button that wraps a native `<button>` element and applies
 * consistent sizing, variants, and accessibility. Content is projected via
 * `<ng-content>` so you can place text, icons, or both.
 *
 * **Features**
 * - Size presets: `xs`, `sm`, `default`, `lg`, `xl`
 * - Visual variants: `primary`, `secondary`, `outline`, `plain`
 * - Disabled state styling & pointer-lock
 * - Focus-visible outline for keyboard accessibility
 * - Optional `ariaLabel` for icon-only buttons
 *
 * **Inputs**
 * - `size: 'xs' | 'sm' | 'default' | 'lg' | 'xl'`
 *   Controls height and padding. Defaults to `'default'`.
 * - `type: 'primary' | 'secondary' | 'outline' | 'plain'`
 *   Visual style variant. Defaults to `'primary'`.
 * - `disabled: boolean`
 *   Disables the button and applies muted styling. Defaults to `false`.
 * - `ariaLabel?: string`
 *   Accessible label for icon-only or ambiguous buttons.
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
 * <tc-button type="primary">Save changes</tc-button>
 *
 * <!-- Secondary, large -->
 * <tc-button type="secondary" size="lg">Continue</tc-button>
 *
 * <!-- Outline, small -->
 * <tc-button type="outline" size="sm">Learn more</tc-button>
 *
 * <!-- Plain (text-only look) -->
 * <tc-button type="plain">Cancel</tc-button>
 *
 * <!-- Disabled -->
 * <tc-button type="primary" [disabled]="true">Processing…</tc-button>

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
    e.stopPropagation(); // Stop unwanted native click events
    this.onClick.emit();
  }

}

import {Component, Input} from '@angular/core';

/**
 * @component BadgeComponent
 * @selector tc-badge
 * @description
 * A reusable badge component that can be rendered as a `<span>` (default),
 * `<button>`, or `<a>` link.
 * Badges are useful for status indicators, tags, or interactive labels.
 *
 * **Features**
 * - Render as `span` (default), `button`, or `link` via the `type` input
 * - Supports a set of predefined colors for consistent styling
 * - Emits click events when `type="button"` or `type="span"` with `onClick` handler
 * - Accepts projected content (`<ng-content>`) for the badge text or elements
 * - Provides base `.badge` class and color-specific modifier classes (e.g. `.badge-blue`)
 * - Safe default (`span`) ensures you can drop it into any layout
 *
 * **Inputs**
 * - `color: BadgeColor`
 *   Sets the badge color. Available values:
 *   `'red' | 'orange' | 'yellow' | 'green' | 'blue' | 'purple' | 'pink' | 'gray'`
 *
 * - `type: 'link' | 'button' | 'span'`
 *   Determines the element type:
 *   - `"span"` → non-interactive badge (default)
 *   - `"button"` → interactive badge as a `<button>`
 *   - `"link"` → interactive badge as an `<a>`
 *
 * - `href?: string`
 *   Required when `type="link"`. Sets the target URL for the badge link.
 *   When `type="link"`, you must provide `href`; otherwise the `<a>` renders with `href=""` and redirects to the current page.
 *
 * - `onClick?: (event: MouseEvent) => void`
 *   Optional click handler for `type="button"` or `type="span"`.
 *
 * @example
 * ```html
 * <!-- Span badge (default) -->
 * <tc-badge color="yellow">Span Content</tc-badge>
 *
 * <!-- Link badge -->
 * <tc-badge type="link" href="/docs" color="blue">Link Content</tc-badge>
 *
 * <!-- Button badge -->
 * <tc-badge type="button" (click)="refresh($event)" color="red">Button Content</tc-badge>
 * ```
 */

export type BadgeColor = 'red' | 'orange' | 'yellow' | 'green' | 'blue' | 'purple' | 'pink' | 'gray';

@Component({
  selector: 'tc-badge',
  templateUrl: './badge.component.html',
  styleUrls: ['./badge.component.scss']
})
export class BadgeComponent {
  @Input() color: BadgeColor = 'gray';
  @Input() href?: string;
  @Input() type: 'link' | 'button' | 'span' = 'span';
  @Input() onClick?: (e: MouseEvent) => void;

  get colorClass() {
    return ['badge', `badge-${this.color}`];
  }
  handleClick(event: MouseEvent) {
    if (this.onClick) this.onClick(event);
  }
}

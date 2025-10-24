import {Component, Input} from '@angular/core';

/**
 * @component IconComponent
 * @selector tc-icon
 * @description
 * A design-system icon wrapper component that can be rendered as a `<span>` (default),
 * `<button>`, or `<a>` link. It applies consistent sizing and color variants to any
 * projected content (FontAwesome icons, SVGs, images, etc.).
 *
 * **Features**
 * - Render as `span` (default), `button`, or `link` via the `type` input
 * - Size presets: `sm` (12px), `md` (16px), `lg` (20px), `xl` (24px)
 * - Flexible content projection – works with FontAwesome, SVG, or inline images
 * - Color variants: `primary`, `secondary`, `white`, `gray`, `success`, `info`, `warning`, `error`
 * - Accessible with optional `ariaLabel`
 * - Automatically removes default browser styles when rendered as `<button>` or `<a>`
 *
 * **Inputs**
 * - `size: 'sm' | 'md' | 'lg' | 'xl' | 'inherit'`
 *   Controls icon size. Defaults to `'inherit'`.
 *   - sm → 12px
 *   - md → 16px
 *   - lg → 20px
 *   - xl → 24px
 *   - inherit → inherits from parents font size
 *
 * - `color?: 'primary' | 'secondary' | 'white' | 'gray' | 'success' | 'info' | 'warning' | 'error'`
 *   Optional color variant. Defaults to `'primary'` if not set.
 *
 * - `ariaLabel?: string`
 *   Accessible label for screen readers. Recommended when the icon has no visible text.
 *
 * - `type: 'span' | 'button' | 'link'`
 *   Determines the rendered element. Defaults to `'span'`.
 *   - `"span"` → non-interactive icon (default)
 *   - `"button"` → semantic button icon (clickable, with reset styles)
 *   - `"link"` → icon inside an anchor `<a>`
 *
 * - `href?: string`
 * - `routerLink?: any[] | string`
 *   Required when `type="link"`. Sets the target URL/route.
 *
 * - `onClick?: (event: MouseEvent) => void`
 *   Optional click handler for `type="button"` or `type="span"`.
 *
 * **Examples**
 * ```html
 * <!-- FontAwesome icon as span -->
 * <tc-icon size="md" color="primary" ariaLabel="Home">
 *   <i class="fas fa-home"></i>
 * </tc-icon>
 *
 * <!-- SVG icon as button -->
 * <tc-icon type="button" size="lg" color="success" (onClick)="save()">
 *   <svg>...</svg>
 * </tc-icon>
 *
 * <!-- Icon as link -->
 * <tc-icon type="link" href="/settings" size="sm" color="info" ariaLabel="Settings">
 *   <i class="fas fa-cog"></i>
 * </tc-icon>
 *
 * <tc-icon type="link" [routerLink]="['/list', 123]" size="sm" color="info" ariaLabel="Settings">
 *   <i class="fas fa-cog"></i>
 * </tc-icon>
 * ```
 */

@Component({
  selector: 'tc-icon',
  templateUrl: './tc-icon.component.html',
  styleUrls: ['./tc-icon.component.scss']
})
export class TcIconComponent {
  @Input() size: 'sm' | 'md' | 'lg' | 'xl' | 'inherit' = 'inherit';
  @Input() color?: 'primary' | 'secondary' | 'white' | 'gray' | 'success' | 'info' | 'warning' | 'error' = 'primary';
  @Input() ariaLabel?: string;

  @Input() type: 'link' | 'button' | 'span' = 'span';
  @Input() href?: string;
  @Input() routerLink?: any[] | string | null;
  @Input() onClick?: (e: MouseEvent) => void;

  get classList(): string[] {
    return [`tc-icon`, `icon-${this.size}`, `icon-${this.color}`];
  }

  handleClick(event: MouseEvent) {
    if (this.onClick) this.onClick(event);
  }
}

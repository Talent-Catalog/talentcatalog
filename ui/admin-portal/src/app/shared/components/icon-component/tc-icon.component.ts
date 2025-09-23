import {Component, Input} from '@angular/core';

/**
 * @component IconComponent
 * @selector tc-icon
 * @description
 * A design-system icon wrapper component that applies consistent sizing and margins
 * to any projected content (FontAwesome icons, SVGs, images, etc.).
 *
 * **Features**
 * - Size presets: `sm` (12px), `md` (16px), `lg` (20px), `xl` (24px)
 * - Automatic margin calculation based on size
 * - Flexible content projection – works with any icon type
 * - Color variants: `primary`, `secondary`, `white`, `gray`, `success`, `info`, `warning`, `error`
 * - Accessible with optional `ariaLabel`
 *
 * **Inputs**
 * - `size: 'sm' | 'md' | 'lg' | 'xl'`
 *   Controls icon size and margin. Defaults to `'lg'`.
 *   - sm: 12px icon, 16px with margin
 *   - md: 16px icon, 20px with margin
 *   - lg: 20px icon, 24px with margin
 *   - xl: 24px icon, 28px with margin
 * - `color?: 'primary' | 'secondary' | 'white' | 'gray' | 'success' | 'info' | 'warning' | 'error'`
 *   Optional color variant. Defaults to `'primary'` if not set.
 * - `ariaLabel?: string`
 *   Accessible label for screen readers.
 *
 * @examples
 * ```html
 * <!-- FontAwesome icon -->
 * <tc-icon size="md" color="primary" ariaLabel="Home">
 *   <i class="fas fa-home"></i>
 * </tc-icon>
 *
 * <!-- SVG icon -->
 * <tc-icon size="lg" color="success">
 *   <svg>...</svg>
 * </tc-icon>
 * ```
 */

@Component({
  selector: 'tc-icon',
  templateUrl: './tc-icon.component.html',
  styleUrls: ['./tc-icon.component.scss']
})
export class TcIconComponent {
  @Input() size: 'sm' | 'md' | 'lg' | 'xl' = 'lg';
  @Input() color?: 'primary' | 'secondary' | 'white' | 'gray' | 'success' | 'info' | 'warning' | 'error' = 'primary';
  @Input() ariaLabel?: string;

  get classList(): string[] {
    const classes = [`icon-${this.size}`];
    if (this.color) {
      classes.push(`icon-${this.color}`);
    }
    return classes;
  }
}

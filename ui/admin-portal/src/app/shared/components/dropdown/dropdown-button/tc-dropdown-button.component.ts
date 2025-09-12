import {Component, Input} from '@angular/core';

/**
 * @component TcDropdownButtonComponent
 * @description
 * Styled toggle button projected into `<tc-dropdown>`. Internally renders a `<tc-button>`
 * and is meant to live inside the parent dropdown’s `ngbDropdownToggle` wrapper so clicks
 * toggle the menu.
 *
 * **Features**
 * - Uses your design-system `<tc-button>` for consistent styling
 * - Supports button style variants via `type`
 * - Accepts extra classes through `buttonClass`
 * - Accessible labeling via `ariaLabel` (important for icon-only toggles)
 * - Disables interaction with `disabled`
 *
 * @selector tc-dropdown-button
 *
 * @inputs
 * - `type: 'primary' | 'secondary' | 'outline' | 'plain' = 'plain'` — visual variant passed to `<tc-button>`
 * - `buttonClass: string = ''` — extra CSS classes applied to the `<tc-button>`
 * - `ariaLabel: string = ''` — accessible name (required if content is icon-only)
 * - `disabled: boolean = false` — disables the toggle
 *
 * @usageNotes
 * - Place `<tc-dropdown-button>` inside `<tc-dropdown>`; the parent wraps it with `ngbDropdownToggle`.
 *   The toggle behavior is handled by the parent (clicks bubble from `<tc-button>` to the wrapper).
 * - Prefer a visible text label; if the content is only an icon, set `ariaLabel`.
 *
 * @example Basic
 * ```html
 * <tc-dropdown>
 *   <tc-dropdown-button type="primary">Actions</tc-dropdown-button>
 *   <tc-dropdown-menu>
 *     <tc-dropdown-item routerLink="/profile">Profile</tc-dropdown-item>
 *   </tc-dropdown-menu>
 * </tc-dropdown>
 * ```
 *
 * @example Disabled
 * ```html
 * <tc-dropdown>
 *   <tc-dropdown-button [disabled]="true">Menu</tc-dropdown-button>
 *   <tc-dropdown-menu>
 *     <tc-dropdown-item>Item</tc-dropdown-item>
 *   </tc-dropdown-menu>
 * </tc-dropdown>
 * ```
 */

@Component({
  selector: 'tc-dropdown-button',
  templateUrl: './tc-dropdown-button.component.html',
  styleUrls: ['./tc-dropdown-button.component.scss']
})
export class TcDropdownButtonComponent {
  @Input() type: 'primary' | 'secondary' | 'outline' | 'plain' = 'plain';
  @Input() buttonClass: string = '';
  @Input() ariaLabel: string = '';
  @Input() disabled: boolean = false;
}

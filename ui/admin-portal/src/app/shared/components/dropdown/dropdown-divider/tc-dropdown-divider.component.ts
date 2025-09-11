import {Component} from '@angular/core';

/**
 * @component TcDropdownDividerComponent
 * @description
 * A visual separator for grouping items inside `<tc-dropdown>`. Mirrors
 * Bootstrap’s `.dropdown-divider` behavior.
 *
 * **Features**
 * - Non-interactive, purely presentational separator
 * - Helps group related `<tc-dropdown-item>` entries
 *
 * @selector tc-dropdown-divider
 *
 * @usageNotes
 * - Use only inside a dropdown menu (i.e., within `<tc-dropdown>`).
 * - Do not put text inside the divider; screen readers shouldn’t announce it.
 *
 * @example
 * ```html
 * <tc-dropdown>
 *   <tc-dropdown-button>
 *     <button class="btn btn-outline-primary">Menu</button>
 *   </tc-dropdown-button>
 *
 *   <tc-dropdown-menu>
 *     <tc-dropdown-item routerLink="/profile">Profile</tc-dropdown-item>
 *     <tc-dropdown-item routerLink="/settings">Settings</tc-dropdown-item>
 *     <tc-dropdown-divider></tc-dropdown-divider>
 *     <tc-dropdown-item (itemSelect)="logout()">Logout</tc-dropdown-item>
 *   </tc-dropdown-menu>
 * </tc-dropdown>
 * ```
 */


@Component({
  selector: 'tc-dropdown-divider',
  templateUrl: './tc-dropdown-divider.component.html',
  styleUrls: ['./tc-dropdown-divider.component.scss']
})
export class TcDropdownDividerComponent {

}

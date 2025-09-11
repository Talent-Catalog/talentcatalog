import {Component, Input} from '@angular/core';

/**
 * @component TcDropdownMenuComponent
 * @description
 * Projection-only wrapper that groups dropdown content inside `<tc-dropdown>`.
 * It does **not** render its own container; the actual `.dropdown-menu` is
 * provided by `TcDropdownComponent`. Use this component to organize items and,
 * optionally, provide extra classes for the parent menu container.
 *
 * **Features**
 * - Projects `<tc-dropdown-item>` and `<tc-dropdown-divider>`
 * - Optional `menuClass` to style the parent menu container (width, padding, etc.)
 *
 * @selector tc-dropdown-menu
 *
 * @projectedSelectors
 * - `tc-dropdown-item` — individual actions/links
 * - `tc-dropdown-divider` — visual separator for groups of items
 *
 * @inputs
 * - `menuClass: string | string[]` — extra classes to be applied to the
 *   parent menu container (handled by `TcDropdownComponent`)
 *
 * @usageNotes
 * - Use only as a child of `<tc-dropdown>`.
 * - The parent component is responsible for reading `menuClass` and applying it
 *   to the `.dropdown-menu` container (see example below).
 *
 * @example Basic
 * ```html
 * <tc-dropdown>
 *   <tc-dropdown-button>
 *     <button class="btn btn-outline-primary">Actions</button>
 *   </tc-dropdown-button>
 *
 *   <tc-dropdown-menu>
 *     <tc-dropdown-item routerLink="/profile">Profile</tc-dropdown-item>
 *     <tc-dropdown-item (click)="doThing()">Do a thing</tc-dropdown-item>
 *     <tc-dropdown-divider></tc-dropdown-divider>
 *     <tc-dropdown-item (click)="logout()">Logout</tc-dropdown-item>
 *   </tc-dropdown-menu>
 * </tc-dropdown>
 * ```
 *
 */

@Component({
  selector: 'tc-dropdown-menu',
  templateUrl: './tc-dropdown-menu.component.html',
  styleUrls: ['./tc-dropdown-menu.component.scss']
})
export class TcDropdownMenuComponent {
  @Input() menuClass: string | string[] = '';
}

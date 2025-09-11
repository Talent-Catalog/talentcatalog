import {Component, Input} from '@angular/core';
import {Placement} from '@ng-bootstrap/ng-bootstrap';

/**
 * @component TcDropdownComponent
 * @description
 * A lightweight, accessible wrapper around **ng-bootstrap**'s dropdown that uses
 * content projection for the toggle, menu container, items, and dividers.
 *
 * **Features**
 * - Projects a custom toggle via `<tc-dropdown-button>`
 * - Projects a custom menu wrapper via `<tc-dropdown-menu>`
 * - Projects menu content via `<tc-dropdown-item>` and `<tc-dropdown-divider>`
 * - Supports `placement` (from ng-bootstrap), start/end alignment, and extra menu classes
 *
 * @selector tc-dropdown
 *
 * @projectedSelectors
 * - `tc-dropdown-button` — the clickable/tappable toggle content
 * - `tc-dropdown-menu` — an optional wrapper for grouping items
 * - `tc-dropdown-item` — individual actionable items/links
 * - `tc-dropdown-divider` — visual separator between groups of items
 *
 * @inputs
 * - `placement: Placement = 'bottom-start'` — where the menu appears relative to the toggle
 * - `align: 'start' | 'end' = 'start'` — adds `.dropdown-menu-start` or `.dropdown-menu-end`
 * - `menuClass: NgClass` — extra classes merged onto the menu container
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
 */

@Component({
  selector: 'tc-dropdown',
  templateUrl: './tc-dropdown.component.html',
  styleUrls: ['./tc-dropdown.component.scss'],
})
export class TcDropdownComponent {
  @Input() placement: Placement = 'bottom-start';
  @Input() menuClass: any;
  @Input() container: string | null = 'body';
  @Input() align: 'start' | 'end' = 'start';
}

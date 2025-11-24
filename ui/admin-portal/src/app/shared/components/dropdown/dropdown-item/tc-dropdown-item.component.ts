import {Component, EventEmitter, Input, Output} from '@angular/core';

/**
 * @component TcDropdownItemComponent
 * @description
 * A flexible dropdown action item used inside `<tc-dropdown>`. Renders as:
 * - a **button** (default),
 * - an **anchor** with `href`, or
 * - an **Angular RouterLink**,
 * while normalizing click handling via the `(itemSelect)` output and supporting
 * a disabled state.
 *
 * **Features**
 * - Auto-selects element type (button / `<a href>` / `<a [routerLink]>`)
 * - Unified click output via `(itemSelect)`
 * - Disables interaction and prevents default when `disabled`
 * - Supports additional CSS classes via `itemClass`
 *
 * @selector tc-dropdown-item
 *
 * @inputs
 * - `href?: string` — when provided, renders an `<a>` with `href`
 * - `routerLink?: string | any[]` — when provided, renders an `<a [routerLink]>`
 * - `disabled: boolean = false` — prevents selection and disables UI
 * - `itemClass: any = ''` — extra CSS classes applied to the rendered element
 *
 * @outputs
 * - `itemSelect: EventEmitter<void>` — emitted on user activation (click/enter)
 *
 * @example Button (default)
 * ```html
 * <tc-dropdown-item (itemSelect)="doAction()">Do action</tc-dropdown-item>
 * ```
 */

@Component({
  selector: 'tc-dropdown-item',
  templateUrl: './tc-dropdown-item.component.html',
})
export class TcDropdownItemComponent {
  @Input() href?: string;
  @Input() target?: string;
  @Input() routerLink?: string | any[];
  @Input() disabled = false;
  @Input() itemClass: any = '';

  @Output() itemSelect = new EventEmitter<void>();

  emit(event?: Event) {
    if (this.disabled) {
      event?.preventDefault();
      return;
    }
    this.itemSelect.emit();
  }
}
